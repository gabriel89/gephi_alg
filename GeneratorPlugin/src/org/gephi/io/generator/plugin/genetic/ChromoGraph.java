/**
 * Creates a chromosome-evolved social network
 */
package org.gephi.io.generator.plugin.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.graph.api.*;
import org.gephi.io.generator.plugin.AbstractGraph;
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
 * ChromoGraph algorithm:................................................. <br>
 * 1. Select a solution representation for the chromosome (string) ....... <br>
 * 2. Place N nodes evenly in a geographic space of size RxR and.......... <br>
 * .. initialize each node with a random chromosome string ............... <br>
 * 3. For each iteration: ................................................ <br>
 * .. In a temporary set: ................................................ <br>
 * .. a. Select all pairs of nodes and add an edge with p_link/distance .. <br>
 * ..... probability ..................................................... <br>
 * .. b. Create a child node (new node) linked to both parents at position <br>
 * ..... (k1P1,k2P2) where k1/k2 = fitness ratio of parents and the child .<br>
 * ..... chromosome is e% copy of random parent, c% crossover P1|P2, ..... <br>
 * ..... m% mutation of random parent P1|P2. ............................. <br>
 * .. c. Measure fitness of all added children ........................... <br>
 * .. d. All children with fitness > threshold remain and are added to ... <br>
 * ..... population ...................................................... <br>
 * 4. Stop after # iterations or at # population ......................... <br>
 *
 * @author Alexandru Topirceanu
 *
 */
@ServiceProvider(service = Generator.class)
public class ChromoGraph extends AbstractGraph implements Generator {

    private int numberOfNodes = 50;
    private int radius = 1000;
    private double linkProbability = 0.1;
    private int gSteps = 8;
    private double fitnessThreshold = 0.85;
    private String solution = "Hello World"; //"I know something you do not!";
    private double pGen_Elite = 0.15;
    private double pGen_Mutate = 0.02;
    private ArrayList<Node> nodes;
    public static final String NODE_CHROMOSOME = "node_chromo";
    public static final String NODE_FITNESS = "node_fit";

    @Override
    protected int initialize() {
        return numberOfNodes * gSteps;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        nodes = new ArrayList<Node>();
        Graph graph = graphModel.getGraph();

        /**
         * 1, 2 Initialize nodes in area
         */
        // position inside area
        for (int i = 0; i < numberOfNodes; ++i) {
            // generate random (x, y)
            int x = radius;
            int y = radius;

            while (x * x + y * y > radius * radius) {
                x = random.nextInt(radius) - radius / 2;
                y = random.nextInt(radius) - radius / 2;
            }

            // create node
            Node node = graphModel.factory().newNode();
            // initialize node
            node.getNodeData().setSize(NODE_SIZE);
            node.getNodeData().setLabel("" + (i));
            // initialize chromosome
            node.getAttributes().setValue(NODE_CHROMOSOME, getRandomSolution(random, solution));
            node.getAttributes().setValue(NODE_FITNESS, getFitness(solution, (String) node.getAttributes().getValue(NODE_CHROMOSOME)));
            // add to graph
            graphModel.getGraph().addNode(node);
            nodes.add(node);
            node.getNodeData().setX(x);
            node.getNodeData().setY(y);

            //Sleep some time
            animateNode();
            progressTick();
        }

        progress.switchToIndeterminate();

        /**
         * 3. Run genetic optimization
         */
        // temporary population
        ArrayList<Node> gNodes = new ArrayList<Node>();
        // repeat process
        for (int g = 0; g < gSteps && !cancel; ++g) {

            // order in descending order of fitness
            Collections.sort(nodes, new NodeComparator(NODE_FITNESS, false));

            // select top elite% for crossover -> m           
            for (int i = 0; i < (nodes.size() * pGen_Elite); ++i) {
                gNodes.add(nodes.get(i));
            }

            // apply crossover on m -> m'     
            int i = nodes.size();
            for (int a = 0; a < (nodes.size() * pGen_Elite) - 1; ++a) {
                for (int b = a + 1; b < (nodes.size() * pGen_Elite); ++b) {
                    Node source = nodes.get(a);
                    Node target = nodes.get(b);

                    if (source.equals(target)) {
                        continue;
                    }

                    // get distance between nodes
                    double distance = getDistance(source, target);

                    // check probability, distance
                    double p = random.nextDouble();
                    if (p <= (linkProbability / (distance/radius))) {

                        // 2 children for each pair
                        Node[] children = createNodes(graphModel, source, target, i, random);
                        ArrayList<Node> goodChildren = new ArrayList<Node>();

                        for (Node child : children) {
                            if ((Double) child.getAttributes().getValue(NODE_FITNESS) > fitnessThreshold) {
                                goodChildren.add(child);
                                gNodes.add(child);
                                graph.addNode(child);

                                // link parents
                                createUndirectedEdge(graphModel, source, target);
                                // connect to child
                                createUndirectedEdge(graphModel, source, child);
                                createUndirectedEdge(graphModel, target, child);
                            }
                        }

                        // connect brothers
//                        for (int c1 = 0; c1 < goodChildren.size() - 1; ++c1) {
//                            for (int c2 = c1 + 1; c2 < goodChildren.size(); ++c2) {
//                                createUndirectedEdge(graphModel, goodChildren.get(c1), goodChildren.get(c2));
//                            }
//                        }

                        i += goodChildren.size();
                        goodChildren.clear();
                        goodChildren = null;
                    }
                }
                progressTick();
            }

            // mutate (m+m') -> m*
            for (Node node : gNodes) {
                double pm = random.nextDouble();

                if (pm <= pGen_Mutate) {
                    String chromosome = (String) node.getAttributes().getValue(NODE_CHROMOSOME);
                    chromosome = getMutatedSolution(random, chromosome);
                    node.getAttributes().setValue(NODE_CHROMOSOME, chromosome);
                    node.getAttributes().setValue(NODE_FITNESS, getFitness(solution, chromosome));
                }
            }

            // merge mo with m* -> m1
            for (Node node : gNodes) {
                if (!nodes.contains(node)) {
                    nodes.add(node);
                }
            }
            gNodes.clear();
        }

        progress.switchToDeterminate(
                100);

        /*
         // 2. Run genetic algorithm 'g' times
         for (int g = 0; g < gSteps && !cancel; ++g) {

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
         int sBest = (int) (gEdges.size() * pGen_Elite);
         int sCross = (int) (gEdges.size() * pGen_Cross);
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

         progress.switchToDeterminate(100);*/
    }
// <editor-fold defaultstate="collapsed" desc="Utility">
    private final int minAscii = 32;
    private final int maxAscii = 126;

    /**
     * Create a random string solution of size solution.length
     *
     * @param rand
     * @param solution
     * @return
     */
    private String getRandomSolution(Random rand, String baseSolution) {

        char[] solution = new char[baseSolution.length()];

        // ascii [32, 126]
        for (int i = 0; i < solution.length; ++i) {
            solution[i] = (char) (rand.nextInt(maxAscii - minAscii + 1) + minAscii);
        }

        return new String(solution);
    }

    private String getMutatedSolution(Random rand, String chromosome) {

        // get random letter
        int index = rand.nextInt(chromosome.length());
        char c = chromosome.charAt(index);

        // mutate one bit of the letter iff ascii [32-126]    
        while ((int) c < minAscii || (int) c > maxAscii) {
            int rx = rand.nextInt(8);
            c ^= (1 << rx);
        }

        // reconstruct new chromosome
        String newChromosome = index < chromosome.length()
                ? chromosome.substring(0, index) + c + chromosome.substring(index + 1)
                : chromosome.substring(0, index) + c;

        return newChromosome;
    }

    private String[] getCrossoverSolutions(Random rand, String sol1, String sol2) {

        // get random crossover index
        int index = rand.nextInt(sol1.length());

        // reconstruct new chromosome
        String chromosome1 = index < sol1.length()
                ? sol1.substring(0, index) + sol2.substring(index)
                : sol1.substring(0, index);

        String chromosome2 = index < sol1.length()
                ? sol2.substring(0, index) + sol1.substring(index)
                : sol2.substring(0, index);

        return new String[]{chromosome1, chromosome2};
    }

    /**
     * Compute the string-distance between the two solutions
     *
     * @param solution
     * @param baseSolution
     * @return
     */
    private double getFitness(String solution, String baseSolution) {

        if (solution.length() != baseSolution.length()) {
            throw new IllegalArgumentException("Solutions do not math in length");
        }

        double sum = 0.0;
        for (int i = 0; i < solution.length(); ++i) {
            char c1 = solution.charAt(i);
            char c2 = baseSolution.charAt(i);

            // normalized as distance between characters / maximum distance
            double dist = 1.0 * Math.abs((int) c1 - (int) c2) / (maxAscii - minAscii);
            sum += dist;
        }

        // compute fitness as inversed average of distances
        double fitness = (1.0 - sum / solution.length());
        return fitness;
    }

    /*
     * Get euclidean distance between nodes
     */
    private double getDistance(Node n1, Node n2) {

        double x1 = n1.getNodeData().x();
        double y1 = n1.getNodeData().y();
        double x2 = n2.getNodeData().x();
        double y2 = n2.getNodeData().y();
        double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

        return dist;
    }

    private Node[] createNodes(GraphModel graphModel, Node source, Node target, int i, Random random) {
        // create nodes
        Node node0 = graphModel.factory().newNode();
        Node node1 = graphModel.factory().newNode();
        // initialize nodes
        node0.getNodeData().setSize(NODE_SIZE);
        node0.getNodeData().setLabel("" + (i));
        node1.getNodeData().setSize(NODE_SIZE);
        node1.getNodeData().setLabel("" + (i + 1));

        // chromosome
        String[] chromosomes;

        // get chromosomes
        String sol1 = (String) source.getAttributes().getValue(NODE_CHROMOSOME);
        String sol2 = (String) target.getAttributes().getValue(NODE_CHROMOSOME);

        // create crossover
        chromosomes = getCrossoverSolutions(random, sol1, sol2);

        node0.getAttributes().setValue(NODE_CHROMOSOME, chromosomes[0]);
        node0.getAttributes().setValue(NODE_FITNESS, getFitness(solution, chromosomes[0]));
        node1.getAttributes().setValue(NODE_CHROMOSOME, chromosomes[1]);
        node1.getAttributes().setValue(NODE_FITNESS, getFitness(solution, chromosomes[1]));

        // position 

        // get fitnesses
        double fit0 = (Double) source.getAttributes().getValue(NODE_FITNESS);
        double fit1 = (Double) target.getAttributes().getValue(NODE_FITNESS);
        // compute parent wieghts
        double k0 = fit0 / (fit0 + fit1);
        double k1 = fit1 / (fit0 + fit1);
        // compute child weighted position
        float x = (float) (k0 * source.getNodeData().x() + k1 * target.getNodeData().x()) / 2;
        float y = (float) (k0 * source.getNodeData().y() + k1 * target.getNodeData().y()) / 2;
        node0.getNodeData().setX(x);
        node0.getNodeData().setY(y);

        x = (float) (k1 * source.getNodeData().x() + k0 * target.getNodeData().x()) / 2;
        y = (float) (k1 * source.getNodeData().y() + k0 * target.getNodeData().y()) / 2;
        node1.getNodeData().setX(x);
        node1.getNodeData().setY(y);

        return new Node[]{node0, node1};
    }
   
    public class NodeComparator implements Comparator<Node> {

        private String toCompare;
        private boolean ascending;
        private Node reference;

        public NodeComparator(String toCompare, boolean ascending) {
            this.toCompare = toCompare;
            this.ascending = ascending;

            reference = null;
        }

        public NodeComparator(String toCompare, boolean ascending, Node reference) {
            this(toCompare, ascending);

            this.reference = reference;
        }

        public int compare(Node node1, Node node2) {

            Double f1 = (Double) node1.getAttributes().getValue(toCompare);
            Double f2 = (Double) node2.getAttributes().getValue(toCompare);

            if (f1 == null || f2 == null) {
                return 0;
            }

            // compare a 'statistic' of each node
            if (ascending) {
                return f1.compareTo(f2);
            } else {
                return f2.compareTo(f1);
            }
        }
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters/Setters">

    public String getName() {
        return NbBundle.getMessage(ChromoGraph.class, "ChromoGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(ChromoGraphUI.class);
    }

    public void setNumberOfNodes(int numberOfNodes) {
        if (numberOfNodes < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfNodes = numberOfNodes;
    }

    public void setRadius(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        this.radius = radius;
    }

    public void setGSteps(int gSteps) {
        if (gSteps < 0) {
            throw new IllegalArgumentException("Number of steps must be positive");
        }
        this.gSteps = gSteps;
    }

    public void setFitnessThreshold(double fitnessThreshold) {
        this.fitnessThreshold = fitnessThreshold;
    }

    public void setSolution(String solution) {
        if (solution == null || solution.length() == 0) {
            throw new IllegalArgumentException("Solution string must be non-void");
        }
        this.solution = solution;
    }

    public void setLinkProbability(double linkProbability) {
        if (linkProbability < 0 || linkProbability > 1) {
            throw new IllegalArgumentException("Link probability must be between 0 and 1");
        }
        this.linkProbability = linkProbability;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public double getLinkProbability() {
        return linkProbability;
    }

    public int getRadius() {
        return radius;
    }

    public int getgSteps() {
        return gSteps;
    }

    public double getFitnessThreshold() {
        return fitnessThreshold;
    }

    public String getSolution() {
        return solution;






    }

    // </editor-fold>   
    void testProcess() {

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
