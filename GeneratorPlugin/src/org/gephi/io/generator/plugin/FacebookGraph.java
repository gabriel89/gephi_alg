/**
 * Creates a Facebook ego-network
 */
package org.gephi.io.generator.plugin;

import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.graph.api.*;
import org.gephi.io.generator.spi.Generator;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.impl.PartitionFactory;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class FacebookGraph extends AbstractGraph implements Generator {

    private int numberOfCells = 5;
    private int avgCellSize = 50;
    private double intraWiringProbability = 0.2;
    private double interWiringProbability = 0.2;
    private int KMin = 2;
    private int KMax = 5;
    private boolean prefferentialSW = false;
    private boolean distributedCluster = true;
    private Cell[] nodeArray;

    @Override
    protected int initialize() {
        return 2*numberOfCells;
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

                // generate random x,y               
//                int x = Position;
//                int y = Position;
//
//                while (x * x + y * y > Position * Position) {
//                    x = random.nextInt(Position) - Position / 2;
//                    y = random.nextInt(Position) - Position / 2;
//                }

                // create node
                Node node = graphModel.factory().newNode();
                // initialize node
                node.getNodeData().setSize(NODE_SIZE);
                node.getNodeData().setLabel("" + (size + i));
                //node.getNodeData().setX(x);
                //node.getNodeData().setY(y);
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

            progressTick();
            size += n;
        }

        // genetically connect cells
        SortedSet<Cell> sortedCells = new TreeSet<Cell>();
        for (Cell cell : nodeArray) {
            sortedCells.add(cell);
        }

        for (int cell = 0; cell < numberOfCells && !cancel; ++cell) {
            // for every node in cell
            for (Node source : nodeArray[cell].getNodes()) {
                Edge[] edges = graph.getEdges(source).toArray();
                // for each of the node's edges
                for (Edge edge : edges) {
                    // check probability
                    if (random.nextDouble() < interWiringProbability) {

                        // choose random distributed destination cell
                        Cell otherCell = getDistributedCellSize(graphModel, sortedCells, random, nodeArray[cell], true);
                        // choose preferential targe in random cell
                        Node target = getDistributedNodeSize(graphModel, otherCell, random, false);

                        try {
                            graph.writeLock();
                            graph.removeEdge(edge);
                        } catch (NullPointerException np) {
                            /* ignore */
                        } finally {
                            graph.writeUnlock();
                        }
                        Edge newEdge = graphModel.factory().newEdge(source, target);
                        graph.addEdge(newEdge);

                        //Sleep some time
                        animateNode();
                    }
                }
            }
            progressTick();
        }

        // add leader and diplomats
        for (Cell cell : nodeArray) {
            // get random leader
            Node leader = cell.getNodes().get(random.nextInt(cell.getSize()));
            // a leader connects with 75% to all others in cell
            for (Node other : cell.getNodes()) {
                if (!leader.equals(other)) {
                    if (graph.getEdge(leader, other) == null) {
                        if (random.nextFloat() < 0.75f) {
                            Edge newEdge = graphModel.factory().newEdge(leader, other);
                            graph.addEdge(newEdge);
                        }
                    }
                }
            }
            // color red
            leader.getNodeData().setColor(1, 0, 0);

            // get 10% random diplomats from each cell
            for (int i = 0; i < cell.getSize() / 10; ++i) {
                // select different from leader
                Node diplomat = leader;
                while (diplomat.equals(leader)) {
                    diplomat = cell.getNodes().get(random.nextInt(cell.getSize()));
                }

                // create extra links to other cells
                int degree = graph.getDegree(diplomat);
                // add another 'degree' edges to other cells
                for (int j = 0; j < degree; ++j) {
                    // choose prefferential distributed destination cell
                    Cell otherCell = getDistributedCellSize(graphModel, sortedCells, random, cell, true);
                    // choose random target in cell
                    Node target = diplomat;
                    while (target.equals(diplomat) || graph.getEdge(diplomat, target) != null) {
                        target = getDistributedNodeSize(graphModel, otherCell, random, false);
                    }

                    Edge newEdge = graphModel.factory().newEdge(diplomat, target);
                    graph.addEdge(newEdge);
                }
                // color blue
                diplomat.getNodeData().setColor(0, 0, 1);
            }
        }



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

        testProcess();
    }    

    private Cell getDistributedCellSize(GraphModel graphModel, SortedSet<Cell> sortedCells, Random random, Cell currentCell, boolean isPrefferential) {
        // choose other cell with highest average degree
        if (isPrefferential) {
            double p = random.nextDouble();
            double degree = 0.0;

            for (Cell cell : sortedCells) {
                degree += cell.getMeanDegree(graphModel.getGraph());
            }

            for (Cell cell : sortedCells) {
                double proc = 1.0 * cell.getMeanDegree(graphModel.getGraph()) / degree;

                if (p > proc) {
                    p -= proc;
                } else {
                    return cell;
                }
            }

            return sortedCells.last();

        } // choose random distributed cell other than current cell
        else {
            Cell otherCell = currentCell;
            while (otherCell.equals(currentCell)) {
                otherCell = (Cell) sortedCells.toArray()[random.nextInt(numberOfCells)];
            }
            return otherCell;
        }
    }

    private Node getDistributedNodeSize(GraphModel graphModel, Cell cell, Random random, boolean isPrefferential) {
        // get richest node from cell
        if (isPrefferential) {
            // store best results
            ArrayList<Node> bestNodes = new ArrayList<Node>();
            Node nodeMax = cell.getNodes().get(0);

            int sumpk = 0;
            for (Node node : cell.getNodes()) {
                sumpk += graphModel.getGraph().getDegree(node);
            }

            // try to connect the new node to nodes in the network
            for (Node node : cell.getNodes()) {
                // degree of current sfNode
                int pi = graphModel.getGraph().getDegree(node);

                // get random value
                float p = random.nextFloat();

                // connect if p < probability
                if (p < 1.0 * pi / sumpk) {
                    bestNodes.add(node);
                }

                if (graphModel.getGraph().getDegree(nodeMax) < graphModel.getGraph().getDegree(node)) {
                    nodeMax = node;
                }
            }


            // choose random from best nodes
            if (bestNodes.size() == 0) {
                return nodeMax;
            } else {
                return bestNodes.get(random.nextInt(bestNodes.size()));
            }
        } // get random node
        else {
            return cell.getNodes().get(random.nextInt(cell.getSize()));
        }
    }
// <editor-fold defaultstate="collapsed" desc="Getters/Setters">

    public String getName() {
        return NbBundle.getMessage(FacebookGraph.class, "FacebookGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(FacebookGraphUI.class);
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
        atlas2.setAdjustSizes(true);
        atlas2.setOutboundAttractionDistribution(true);

        atlas2.initAlgo();
        for (int i = 0; i < 1000; ++i) {
            if (atlas2.canAlgo()) {
                atlas2.goAlgo();
            }
        }
        atlas2.endAlgo();

        // run modularity 
        Modularity modularity = new Modularity();
        modularity.setRandom(true);
        modularity.setUseWeight(false);
        modularity.setResolution(1.0);
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
