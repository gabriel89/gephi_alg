/**
 * Creates a genetically-evolved social network
 */
package org.gephi.io.generator.plugin.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.graph.api.*;
import org.gephi.io.generator.plugin.AbstractGraph;
import org.gephi.io.generator.plugin.Cell;
import org.gephi.io.generator.spi.Generator;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.impl.PartitionFactory;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class GeneticGraph extends AbstractGraph implements Generator {

    private int numberOfCells = 5;
    private int avgCellSize = 20;
    private double intraWiringProbability = 0.15;
    private double interWiringProbability = 0.1;
    private int KMin = 2;
    private int KMax = 10;
    private int GSteps = 1;
    private double PGen_Best = 0.5;
    private double PGen_Cross = 0.3;
    private boolean prefferentialSW = false;
    private boolean distributedCluster = false;
    private Cell[] nodeArray;
    public static final String NODE_INDEX = "node_idx";

    @Override
    protected int initialize() {
        return numberOfCells + numberOfCells * avgCellSize;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        int size = 0;
        nodeArray = new Cell[numberOfCells];
        Graph graph = graphModel.getGraph();

        // initialize each cell
        for (int cell = 0; cell < numberOfCells; ++cell) {
            int n = getPowerDistributedIntegerValue(random, avgCellSize / 5, 5 * avgCellSize);// avgCellSize / 2 + random.nextInt(avgCellSize);
            nodeArray[cell] = new Cell(n);

            // add nodes to cell
            for (int i = 0; i < n; i++) {

                // create node
                Node node = graphModel.factory().newNode((size + i) + "");
                // initialize node
                node.getNodeData().setSize(NODE_SIZE);
                node.getNodeData().setLabel("" + (size + i));
                node.getAttributes().setValue(NODE_INDEX, Cell.createIndex(cell, (size + i)));
                // add to graph
                graphModel.getGraph().addNode(node);
                nodeArray[cell].addNode(node);

                //Sleep some time
                animateNode();
            }

            // add intra-cell links to create a small-world
            if (intraWiringProbability > 0) {
                createSmallWorldCommunity(nodeArray[cell], graphModel, random,
                        KMin, KMax, intraWiringProbability, prefferentialSW, distributedCluster);
            }

            size += n;
            progressTick();
        }

        
        // genetically connect cells        
        ArrayList<Edge> gEdges = new ArrayList<Edge>();

        // 1. Initialize G-set:
        // choose nodes according to interCellProbability 
        for (Cell cell : nodeArray) {
            if (cancel) {
                break;
            }
            // for every node in cell
            for (Node source : cell.getNodes()) {
                Edge[] edges = graph.getEdges(source).toArray();
                // for each of the node's edges
                for (Edge edge : edges) {
                    // check probability
                    if (random.nextDouble() < interWiringProbability) {
                        // choose (any) random destination cell                        
                        Cell anyCell = (Cell) getRandomAndDifferent(nodeArray, null, nodeArray.length, random);
                        // choose random node
                        Node target = (Node) getRandomAndDifferent(anyCell.getNodes().toArray(), source, anyCell.getSize(), random); //getDistributedNodeSize(graphModel, otherCell, random, false);

                        try {
                            graph.writeLock();
                            graph.removeEdge(edge);
                        } catch (NullPointerException np) {
                            // ignore
                        } finally {
                            graph.writeUnlock();
                        }
                        Edge newEdge = graphModel.factory().newEdge(source, target);
                        //graph.addEdge(newEdge);

                        // add nodes to genetic set
                        gEdges.add(newEdge);

                        //Sleep some time
                        animateNode();
                    }
                }
                progressTick();
            }
        }

        progress.switchToIndeterminate();
        // 2. Run genetic algorithm 'g' times
        for (int g = 0; g < GSteps && !cancel; ++g) {

            // run betweenness on graph
            GraphDistance statistic = new GraphDistance();
            statistic.setNormalized(false);
            statistic.setDirected(true);
            statistic.setProgressTicket(progress);
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            statistic.execute(graphModel, attributeModel);
            statistic.getDiameter();
            // OR
            // run centrality on graph
//            EigenvectorCentrality statistic2 = new EigenvectorCentrality();
//            statistic2.setNumRuns(100);
//            statistic2.setDirected(true);
//            statistic2.setProgressTicket(progress);
//            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
//            statistic2.execute(graphModel, attributeModel);
//            statistic2.getNumRuns();


            // order edges by descending target's betweenness 
            Collections.sort(gEdges, new EdgeComparator(GraphDistance.BETWEENNESS, false));
//            Collections.sort(gEdges, new EdgeComparator(EigenvectorCentrality.EIGENVECTOR, false));                                

            // genetic settings:
            // 1) keep 60% best solutions
            // 2) crossover 30% of best solutuons
            // 3) mutate 10% of any solution
            int sBest = (int) (gEdges.size() * PGen_Best);
            int sCross = (int) (gEdges.size() * PGen_Cross);
            //int sMutate = gEdges.size() - sBest - sCross;

            int i = 0;
            ArrayList<Edge> gEdges2 = new ArrayList<Edge>();

            // keep best edges
            for (; i < sBest; ++i) {
                gEdges2.add(gEdges.get(i));
            }
            // add crossover between solutions - combine targets to retrieve a new target           
            for (; i < sBest + sCross; ++i) {
                // get two distinct edges from [0, sBest)
//                Edge edge1 = (Edge) getRandomAndDifferent(gEdges.toArray(), null, sBest, random);
//                Edge edge2 = (Edge) getRandomAndDifferent(gEdges.toArray(), edge1, sBest, random);
                // and their target betweenness
//                Double d1 = new SNode(edge1.getTarget()).getValueAsDouble(GraphDistance.BETWEENNESS);
//                Double d2 = new SNode(edge2.getTarget()).getValueAsDouble(GraphDistance.BETWEENNESS);
//                Double d1 = new SNode(edge1.getTarget()).getValueAsDouble(EigenvectorCentrality.EIGENVECTOR);
//                Double d2 = new SNode(edge2.getTarget()).getValueAsDouble(EigenvectorCentrality.EIGENVECTOR);

                Node n1 = gEdges.get(i).getTarget();
                Cell cell = nodeArray[Cell.getCellIdFromIndex(new SNode(n1).getValueAsLong(NODE_INDEX))];
                Node n2 = (Node) getRandomAndDifferent(cell.getNodes().toArray(), n1, cell.getSize(), random);

                Long idx1 = new SNode(n1).getValueAsLong(NODE_INDEX);
                Long idx2 = new SNode(n2).getValueAsLong(NODE_INDEX);

                Long newIdx = Cell.crossoverIndices(random, idx1, idx2);
                Node newNode = graph.getNode(Cell.getNodeIdFromIndex(newIdx));
                if (newNode == null) {
                    newNode = n1;
                    newIdx = idx1;
                }
                newNode.getAttributes().setValue(NODE_INDEX, newIdx);

                // choose best edge target                
//                Edge newEdge = graphModel.factory().newEdge(gEdges.get(i).getSource(),
//                        d1 > d2 ? edge1.getTarget() : edge2.getTarget());
                Edge newEdge = graphModel.factory().newEdge(gEdges.get(i).getSource(),
                        newNode);
                gEdges2.add(newEdge);
            }
            // mutate remainder
            for (; i < gEdges.size(); ++i) {
                // get any random edge
                Edge edge1 = (Edge) getRandomAndDifferent(gEdges.toArray(), null, gEdges.size(), random);
                //Edge edge1 = (Edge) getRandomAndDifferent(gEdges2.subList(0, sBest / 10).toArray(), null, gEdges2.subList(0, sBest / 10).size(), random);

                // choose a better edge target from same cell
                Edge newEdge = graphModel.factory().newEdge(gEdges.get(i).getSource(),
                        edge1.getTarget());
                gEdges2.add(newEdge);
            }

            // copy over
            gEdges.clear();
            gEdges.addAll(gEdges2);

            // clean
            gEdges2.clear();
            gEdges2 = null;
            
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
            }

        }
        // update graph with long-range links
        for (Edge edge : gEdges) {
            graph.addEdge(edge);
        }

        gEdges.clear();
        gEdges = null;

        progress.switchToDeterminate(100);


//        // choose random distributed destination cell
//        Cell otherCell = getDistributedCellSize(graphModel, sortedCells, random, nodeArray[cell], true);
//        // choose preferential targe in random cell
//        Node target = getDistributedNodeSize(graphModel, otherCell, random, false);
//
//        try {
//            graph.writeLock();
//            graph.removeEdge(edge);
//        } catch (NullPointerException np) {
//            /* ignore */
//        } finally {
//            graph.writeUnlock();
//        }
//        Edge newEdge = graphModel.factory().newEdge(source, target);
//        graph.addEdge(newEdge);
//
//        Progress.progress(progress, ++progressUnit);
//        //Sleep some time
//        if (animate) {
//            sleep(animationNodeDelay);
//        }
//    }
//}
//}
//        }
//
//        // add leader and diplomats
//        for (Cell cell : nodeArray) {
//            // get random leader
//            Node leader = cell.getNodes().get(random.nextInt(cell.getSize()));
//            // a leader connects with 75% to all others in cell
//            for (Node other : cell.getNodes()) {
//                if (!leader.equals(other)) {
//                    if (graph.getEdge(leader, other) == null) {
//                        if (random.nextFloat() < 0.75f) {
//                            Edge newEdge = graphModel.factory().newEdge(leader, other);
//                            graph.addEdge(newEdge);
//                        }
//                    }
//                }
//            }
//            // color red
//            leader.getNodeData().setColor(1, 0, 0);
//
//            // get 10% random diplomats from each cell
//            for (int i = 0; i < cell.getSize() / 10; ++i) {
//                // select different from leader
//                Node diplomat = leader;
//                while (diplomat.equals(leader)) {
//                    diplomat = cell.getNodes().get(random.nextInt(cell.getSize()));
//                }
//
//                // create extra links to other cells
//                int degree = graph.getDegree(diplomat);
//                // add another 'degree' edges to other cells
//                for (int j = 0; j < degree; ++j) {
//                    // choose prefferential distributed destination cell
//                    Cell otherCell = getDistributedCellSize(graphModel, sortedCells, random, cell, true);
//                    // choose random target in cell
//                    Node target = diplomat;
//                    while (target.equals(diplomat) || graph.getEdge(diplomat, target) != null) {
//                        target = getDistributedNodeSize(graphModel, otherCell, random, false);
//                    }
//
//                    Edge newEdge = graphModel.factory().newEdge(diplomat, target);
//                    graph.addEdge(newEdge);
//                }
//                // color blue
//                diplomat.getNodeData().setColor(0, 0, 1);
//            }
//        }



        // connect cells
//        for (int i = 0; i < numberOfCells - 1 && !cancel; ++i) {
//            for (int j = i + 1; j < numberOfCells && !cancel; ++j) {
//
//                // add size/2 links between each cell
//                for (int k = 0; k < avgCellSize / 2 && !cancel; ++k) {
//
//                    if (random.nextDouble() < interWiringProbability) {
//
//                        Node source = null;
//                        Node target = null;
//
//                        while (source == null || target == null || graph.getEdge(source, target) != null) {
//                            // cell i                            
//                            int selected = random.nextInt(nodeArray[i].getSize());
//                            source = nodeArray[i].getNodes().get(selected);
//
//                            // cell j                            
//                            selected = random.nextInt(nodeArray[j].getSize());
//                            target = nodeArray[j].getNodes().get(selected);
//                        }
//
//                        Edge edge = graphModel.factory().newEdge(source, target);
//                        graph.addEdge(edge);
//
//                        //Sleep some time
//                        if (animate) {
//                            sleep(animationEdgeDelay);
//                        }
//                    }
//                }
//            }
//        }

        // add geo links
        /*for (Node[] cell1 : nodeArray) {
         for (Node source : cell1) {
         for (Node[] cell2 : nodeArray) {
         for (Node target : cell2) {
         if (!source.equals(target)) {

         // get distance between nodes
         double x1 = source.getNodeData().x();
         double y1 = source.getNodeData().y();
         double x2 = target.getNodeData().x();
         double y2 = target.getNodeData().y();
         double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

         // if inside max distance
         if (dist <= MaxDistance * Position) {

         // check both probabilities and link                        
         float ps = random.nextFloat();
         if (ps <= SocialProb) {
         float pp = random.nextFloat();
         if (pp < ParanoidProb) {
         //graph.writeLock();
         Edge edge = graphModel.factory().newEdge(source, target);
         graph.addEdge(edge);
         //graph.writeUnlock();

         //Sleep some time
         if (animate) {
         sleep(animationEdgeDelay);
         }
         }
         }
         }
         }
         }
         }
         }
         }*/

        //testProcess();      
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getName() {
        return NbBundle.getMessage(GeneticGraph.class, "GeneticGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(GeneticGraphUI.class);
    }

    public void setNumberOfCells(int numberOfCells) {
        if (numberOfCells < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfCells = numberOfCells;
    }

    public void setAvgCellSize(int cellSize) {
        if (cellSize < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.avgCellSize = cellSize;
        this.KMin = (int) (avgCellSize * 0.1);
        this.KMax = (int) (avgCellSize / 2 * 0.8);
    }

    public void setIntraWiringProbability(double intraWiringProbability) {
        if (intraWiringProbability < 0 || intraWiringProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.intraWiringProbability = intraWiringProbability;
    }

    public void setInterWiringProbability(double interWiringProbability) {
        if (interWiringProbability < 0 || interWiringProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.interWiringProbability = interWiringProbability;
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public int getAvgCellSize() {
        return avgCellSize;
    }

    public double getIntraWiringProbability() {
        return intraWiringProbability;
    }

    public double getInterWiringProbability() {
        return interWiringProbability;
    }

    // </editor-fold>   
    public void testProcess() {

        // Get current graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getGraph();
        // run forceatlas2
        ForceAtlas2Builder builder = new ForceAtlas2Builder();
        ForceAtlas2 atlas2 = builder.buildLayout();

        atlas2.setGraphModel(graphModel);

        atlas2.setAdjustSizes(
                true);
        atlas2.setOutboundAttractionDistribution(
                true);

        atlas2.initAlgo();
        for (int i = 0;
                i < 1000; ++i) {
            if (atlas2.canAlgo()) {
                atlas2.goAlgo();
            }
        }

        atlas2.endAlgo();
        // run modularity 
        Modularity modularity = new Modularity();

        modularity.setRandom(
                true);
        modularity.setUseWeight(
                false);
        modularity.setResolution(
                1.0);
        modularity.setProgressTicket(progress);
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        modularity.execute(graphModel, attributeModel);

        modularity.getModularity();
        // color: partition by modularity class
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeTable graphTable = attributeModel.getGraphTable();
        AttributeColumn atrColumn = nodeTable.getColumn(Modularity.MODULARITY_CLASS);
        NodeColorTransformer transformer = new NodeColorTransformer();
        NodePartition partition = PartitionFactory.createNodePartition(atrColumn);

        PartitionFactory.buildNodePartition(partition, graph);

        transformer.randomizeColors(partition);

        transformer.transform(partition);
    }
}
