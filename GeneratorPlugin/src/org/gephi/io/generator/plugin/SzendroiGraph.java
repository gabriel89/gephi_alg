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
 * Creates a realistic social network, as inspired by the Szendroi experiment:
 * http://people.maths.ox.ac.uk/szendroi/microtalk.pdf
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class SzendroiGraph extends ScaleFreeGraph implements Generator {

    private int degree = 15;

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
            Node newNode = createNode(graphModel, i, true);

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

                // try to connect the new node to nodes in the network
                for (Node node : nodeArray) {

                    double[] pi = new double[metrics.length];
                    // metric of current sfNode
                    for (int m = 0; m < metrics.length; ++m) {
                        pi[m] = getNodeMetric(graph, node, metrics[m]);
                    }

                    // get random value
                    double p = random.nextDouble();
                    // weight of each metric
                    double w = 1.0 / metrics.length;

                    double fitness = 0.0;
                    for (int m = 0; m < metrics.length; ++m) {
                        fitness += w * pi[m] / sumpk[m];
                    }

                    // connect if p < probability
                    if (p < fitness) {
                        Edge edge = createUndirectedEdge(graphModel, newNode, node);
                        if (weighted) {
                            double weight = powerLawWeights ? getPowerDistributedDoubleValue(random, 0, 1) : random.nextFloat();
                            edge.setWeight((float) weight);
                        }
                        success = true;
                    }
                }
            }
            nodeArray.add(newNode);

            //  step 2: add new edges to maintain degree ratio
            for (int e = 1; e < degree; ++e) {
                Node randomNode = nodeArray.get(random.nextInt(nodeArray.size()));

                Node[] friends = graph.getNeighbors(randomNode).toArray();
                if(friends.length <= 1)
                {
                    e--;
                    continue;
                }
                Node randomSource = friends[random.nextInt(friends.length)];

                for (Node randomTarget : friends) {
                    if (!randomSource.equals(randomTarget)) {
                        if (existsNoEdge(graphModel, randomSource, randomTarget)) {
                            createUndirectedEdge(graphModel, randomSource, randomTarget);
                            break;
                        }
                    }
                }
            }

            progressTick();
        }
        nodeArray = null;
        progress.switchToDeterminate(100);
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getName() {
        return NbBundle.getMessage(SzendroiGraph.class, "SzendroiGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(SzendroiGraphUI.class);
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }
    // </editor-fold>   
}
