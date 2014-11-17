package org.gephi.io.generator.plugin.genetic;

import org.gephi.io.generator.plugin.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.*;
import org.gephi.io.generator.plugin.ScaleFreeGraph.Metric;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Grows a realistic complex network from a seed network
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class GeneticGrowingGraph extends ScaleFreeGraph implements Generator {

    private int numberOfNodes = 100;
    private int numberOfCommunities = 5;
    private boolean useSeedAsReference = false;   
    private int population = 100;
    private HashMap<Metric, Double> referenceMetrics;
    protected Metric[] measuredMetrics = {Metric.Degree, Metric.APL, Metric.Clustering, Metric.Modularity};
    private int avgDeg = 5;

    @Override
    protected int initialize() {
        return numberOfNodes;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        referenceMetrics = new HashMap<Metric, Double>();

        if (useSeedAsReference) {
            // much work to do            
            return;
        } else {
            referenceMetrics.put(Metric.Degree, 1.0 * avgDeg);
            referenceMetrics.put(Metric.APL, 3.25);
            referenceMetrics.put(Metric.Clustering, 0.256);
            referenceMetrics.put(Metric.Modularity, 0.587);
        }

        // nodes
        Cell[] cells = new Cell[numberOfCommunities];
        Graph graph = graphModel.getGraph();
        int size = 0;

        progress.switchToIndeterminate();

        // initialize each cell
        for (int cell = 0; cell < numberOfCommunities; ++cell) {
            cells[cell] = new Cell();

            // create triad
            Node n1 = createNode(graphModel, size++, true);
            Node n2 = createNode(graphModel, size++, true);
            Node n3 = createNode(graphModel, size++, true);
            // add to community
            cells[cell].addNode(n1);
            cells[cell].addNode(n2);
            cells[cell].addNode(n3);
            // create triad edges
            createUndirectedEdge(graphModel, n1, n2);
            createUndirectedEdge(graphModel, n1, n3);
            createUndirectedEdge(graphModel, n2, n3);
        }

        // connect all communities
        for (int i = 0; i < numberOfCommunities - 1; ++i) {
            for (int j = i + 1; j < numberOfCommunities; ++j) {
                // pick random nodes
                Node n1 = cells[i].getNodes().get(random.nextInt(cells[i].getSize()));
                Node n2 = cells[j].getNodes().get(random.nextInt(cells[j].getSize()));

                createUndirectedEdge(graphModel, n1, n2);
            }
        }

        // save nodes to list
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : graph.getNodes()) {
            nodes.add(node);
        }
        // save edge lists (populations)
        ArrayList<GraphSolution> edges = new ArrayList<GraphSolution>();
        ArrayList<GraphSolution> _edges = new ArrayList<GraphSolution>();
//        for (int i = 0; i < population; ++i) {
//            edges.add(new GraphSolution());
//        }
        edges.add(new GraphSolution());
        for (Edge edge : graph.getEdges()) {
            edges.get(0).add(edge);
        }      

        // network growth - one new node at a time        
        for (int i = size; i < numberOfNodes; ++i) {

            // create new node
            Node currentNode = createNode(graphModel, size++, true);            
            currentNode.getNodeData().setColor(1f, 0, 0);
            Node targetNode;

            // add node in P random manners to first R networks
            int r = 1;//Math.min(edges.size(), random.nextInt(population-1)+1);

            // create P populations
            for (int j = 0; j < population; ++j) {

                // pick a random fit solution
                GraphSolution solution = edges.get(random.nextInt(r));
                // set solution to graph
                graph.clearEdges();
                for (Edge edge : solution.getEdges()) {
                    graph.addEdge(edge);
                }

                // pick number of target nodes for current node
                int n = (int) (1.0 * avgDeg + random.nextGaussian());
                // pick n random targets                
                for (int k = 0; k < n; ++k) {
                    boolean success = false;
                    while (!success) {
                        targetNode = nodes.get(random.nextInt(nodes.size()));
                        success = createUndirectedEdge(graphModel, currentNode, targetNode) != null;
                    }
                }
                
                // save solution
                _edges.add(new GraphSolution());
                for (Edge edge : graph.getEdges()) {
                    _edges.get(j).add(edge);
                }

                // compute fidelity on temporary graph
                HashMap<Metric, Double> metrics = getGraphMetrics(graphModel, measuredMetrics);                
                _edges.get(j).setFidelity(computeFidelity(referenceMetrics, metrics));

                
            }
            // end of populations creation

            // clean old edges and renew
            edges.clear();
            for (GraphSolution solution : _edges) {
                edges.add(solution);
            }
            // clean temporary solutions
            _edges.clear();

            // sort solutions by fitness                       
            Collections.sort(edges, new GraphSolutionComparator(false));
            nodes.add(currentNode);
        }

        progress.switchToDeterminate(100);
    }

    protected final HashMap<Metric, Double> getGraphMetrics(GraphModel graphModel, Metric[] measuredMetrics) {

        HashMap<Metric, Double> values = new HashMap<Metric, Double>();
        double value;

        for (Metric metric : measuredMetrics) {

            if (metric.equals(Metric.Degree)) {
                Degree statistic = new Degree();
                statistic.setProgressTicket(progress);
                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                statistic.execute(graphModel, attributeModel);
                value = statistic.getAverageDegree();

            } else if (metric.equals(Metric.APL)) {
                GraphDistance statistic = new GraphDistance();
                statistic.setNormalized(false);
                statistic.setDirected(directed);
                statistic.setProgressTicket(progress);
                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                statistic.execute(graphModel, attributeModel);
                value = statistic.getPathLength();

            } else if (metric.equals(Metric.Clustering)) {
                ClusteringCoefficient statistic = new ClusteringCoefficient();
                statistic.setDirected(directed);
                statistic.setProgressTicket(progress);
                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                statistic.execute(graphModel, attributeModel);
                value = statistic.getAverageClusteringCoefficient();

            } else if (metric.equals(Metric.Modularity)) {
                Modularity statistic = new Modularity();
                statistic.setRandom(true);
                statistic.setResolution(1.0);
                statistic.setUseWeight(false);
                statistic.setProgressTicket(progress);
                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                statistic.execute(graphModel, attributeModel);
                value = statistic.getModularity();

            } else {
                throw new IllegalArgumentException("Metric not supported: " + metric.toString());
            }

            values.put(metric, value);
        }

        return values;
    }

    protected final double computeFidelity(HashMap<Metric, Double> referenceMetrics, HashMap<Metric, Double> metrics) {
        List<Double> symRatios = new ArrayList<Double>();

        for (Metric metric : referenceMetrics.keySet()) {
            Double m0 = referenceMetrics.get(metric);
            Double m1 = metrics.get(metric);

            if (m0 != null && m1 != null) {
                if (m1 < m0) {
                    symRatios.add(m0 / (2 * m0 - m1));
                } else {
                    symRatios.add(m0 / m1);
                }
            }
        }

        double fidelity = 0.0;
        for (Double sr : symRatios) {
            fidelity += sr;
        }

        fidelity /= symRatios.size();
        return fidelity;
    }

// <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getName() {
        return NbBundle.getMessage(GeneticGrowingGraph.class, "GeneticGrowingGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(GeneticGrowingGraphUI.class);
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public void setNumberOfCommunities(int numberOfCommunities) {
        this.numberOfCommunities = numberOfCommunities;
    }

    public void setAvgDeg(int avgDeg) {
        this.avgDeg = avgDeg;
    }       

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getNumberOfCommunities() {
        return numberOfCommunities;
    }
    
    public int getAvgDeg() {
        return avgDeg;
    }

    // </editor-fold>   

    
}
