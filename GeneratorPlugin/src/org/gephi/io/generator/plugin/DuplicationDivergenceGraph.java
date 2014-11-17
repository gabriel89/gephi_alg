package org.gephi.io.generator.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.gephi.graph.api.*;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Creates a Duplication Divergence model, as defined by
 * http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2092385/
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class DuplicationDivergenceGraph extends ScaleFreeGraph implements Generator {

    private int numberOfNodes = 500;
    private Metric[] metrics = {Metric.Betweenness};
    private final int randomGraphSize = 20;
    private final double pRandomWiring = 0.1;
    private double retention = 0.25;
    private boolean weighted = false;
    private boolean powerLawWeights = false;

    @Override
    protected int initialize() {
        return numberOfNodes;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        // nodes
        List<Node> nodeArray = new ArrayList<Node>(numberOfNodes);
        Graph graph = graphModel.getGraph();

        // initialize random graph seed
        initializeRandomGraph(nodeArray, graph, randomGraphSize, pRandomWiring, weighted, powerLawWeights);

        progress.switchToIndeterminate();

        // initialize other nodes and edges
        for (int i = randomGraphSize; i < numberOfNodes; ++i) {

            // create node
            Node newNode = graphModel.factory().newNode();
            // initialize node
            newNode.getNodeData().setSize(NODE_SIZE);
            newNode.getNodeData().setLabel("" + (i + 1));
            // add to graph
            graph.addNode(newNode);

            /**
             * Define optimization metric(s)
             */
            computeMetricOnGraph(graphModel, metrics);

            // compute sum of all metrics in SF network
            double[] sumpk = new double[metrics.length];
            for (int m = 0; m < metrics.length; ++m) {
                sumpk[m] = 0.0;
                for (Node node : nodeArray) {
                    sumpk[m] += getNodeMetric(graph, node, metrics[m]);
                }
            }

            boolean success = false;
            while (!success) {

                // choose random node from network
                Node parent = nodeArray.get(random.nextInt(nodeArray.size()));

                // try to connect replica to parent neighbors
                for (Node neighbor : graph.getNeighbors(parent).toArray()) {

                    double[] pi = new double[metrics.length];
                    // metric of current sfNode
                    for (int m = 0; m < metrics.length; ++m) {
                        pi[m] = getNodeMetric(graph, neighbor, metrics[m]);
                    }

                    // get random value
                    double p = random.nextDouble();
                    // weight of each metric
                    double w = 1.0 / metrics.length;

                    double fitness = 0.0;
                    for (int m = 0; m < metrics.length; ++m) {
                        fitness += w * pi[m] / sumpk[m];
                    }

                    double r = random.nextDouble();
                    double failure = Math.pow((1 - retention), fitness);

                    if (r < 1- failure) {
                        Edge edge = graphModel.factory().newEdge(newNode, neighbor);                        
                        if(weighted)
                        {
                            float weight = powerLawWeights ? getPowerDistributedIntegerValue(random, 0, 1) : random.nextFloat();      
                            edge.setWeight(weight);
                        }                        
                        graph.addEdge(edge);
                        
                        edge = graphModel.factory().newEdge(neighbor, newNode);
                        if(weighted)
                        {
                            float weight = powerLawWeights ? getPowerDistributedIntegerValue(random, 0, 1) : random.nextFloat();      
                            edge.setWeight(weight);
                        }
                        graph.addEdge(edge);
                        
                        success = true;
                    }
                }
            }
            nodeArray.add(newNode);

            //Sleep some time
            animateNode();
            progressTick();
        }
        nodeArray = null;
        progress.switchToDeterminate(100);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    
    @Override
    public String getName() {
        return NbBundle.getMessage(DuplicationDivergenceGraph.class, "DuplicationDivergenceGraph.name");
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(DuplicationDivergenceGraphUI.class);
    }

    @Override
    public void setNumberOfNodes(int numberOfCells) {
        if (numberOfCells < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfNodes = numberOfCells;
    }

    public void setRetention(double retention) {
        this.retention = retention;
    }

    @Override
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public double getRetention() {
        return retention;
    }
    // </editor-fold>   
}
