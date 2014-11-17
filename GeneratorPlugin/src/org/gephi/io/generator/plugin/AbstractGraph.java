package org.gephi.io.generator.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Abstract Graph - mother of all graphs
 *
 * @author Alexandru Topirceanu
 */
public abstract class AbstractGraph {

    protected boolean clearGraph = true;
    // progress
    protected ProgressTicket progress;
    private int progressTicks, progressState;
    protected boolean cancel = false;
    // animation    
    private boolean animate = false;
    private int animationNodeDelay = 25; // ms
    private int animationEdgeDelay = 1; // ms    
    // other
    protected static final float NODE_SIZE = 5f;

    public void generate(ContainerLoader container) {

        progressTicks = initialize();
        progressState = 0;
        Progress.start(progress, progressTicks);

        //Project
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        ProjectControllerUI projectControllerUI = Lookup.getDefault().lookup(ProjectControllerUI.class);
        if (projectController.getCurrentProject() == null) {
            projectControllerUI.newProject();
        }

        // create graph
        // Get current graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getGraph();

        // clean previous graph        
        if (clearGraph) {
            graph.writeLock();
            if (graph.getEdgeCount() > 0 || graph.getNodeCount() > 0) {
                graph.clear();
            }
            graph.writeUnlock();
        }

        // runs the generation algorithm
        runGeneration(graphModel, new Random());

        Progress.finish(progress);
        progress = null;
    }

    // <editor-fold defaultstate="collapsed" desc="Abstract Hooks">
    /**
     * Implements progress initialization and startup tests. <br> Returns the
     * number of progress ticks.
     */
    protected abstract int initialize();

    /**
     * Runs the specific generate code. <br> Passes the graph model and a random
     * generator.
     */
    protected abstract void runGeneration(GraphModel graphModel, Random random);

    // </editor-fold>
    /**
     * Create a new node with id "i+1"
     *
     * @param add - Whether to add it to the graph
     */
    protected Node createNode(GraphModel graphModel, int i, boolean add) {
        // create node
        Node newNode = graphModel.factory().newNode();
        // initialize node
        newNode.getNodeData().setSize(NODE_SIZE);
        newNode.getNodeData().setLabel("" + (i + 1));
        // add to graph
        if (add) {
            graphModel.getGraph().addNode(newNode);
        }

        //Sleep some time
        animateNode();

        return newNode;
    }

    /**
     * Create a directed edge between the source and target nodes if no edges
     * source->target AND target->source already exist.
     *
     * @return the created edge or null if one existed before
     */
    protected Edge createUndirectedEdge(GraphModel graphModel, Node source, Node target) {

        if (graphModel.getGraph().getEdge(source, target) == null
                && graphModel.getGraph().getEdge(target, source) == null) {

            Edge newEdge = graphModel.factory().newEdge(source, target);
            graphModel.getGraph().addEdge(newEdge);

            //Sleep some time
            animateEdge();

            return newEdge;
        }

        return null;
    }

    /**
     * Create a directed edge between the source and target nodes if no directed
     * edge from source->target exists and add it to the graph
     *
     * @return the created edge or null if one existed before
     */
    protected Edge createAddDirectedEdge(GraphModel graphModel, Node source, Node target) {

        if (graphModel.getGraph().getEdge(source, target) == null) {

            Edge newEdge = graphModel.factory().newEdge(source, target);
            graphModel.getGraph().addEdge(newEdge);

            //Sleep some time
            animateEdge();

            return newEdge;
        }

        return null;
    }

    /**
     * Create a bidirectional edge between the source and target nodes
     *
     * @return
     */
    protected void createBiEdges(GraphModel graphModel, Node source, Node target) {

        if (graphModel.getGraph().getEdge(source, target) == null
                && graphModel.getGraph().getEdge(target, source) == null) {

            Edge newEdge = graphModel.factory().newEdge(source, target);
            graphModel.getGraph().addEdge(newEdge);

            newEdge = graphModel.factory().newEdge(target, source);
            graphModel.getGraph().addEdge(newEdge);

            //Sleep some time
            animateEdge();
        }
    }

    /**
     * Returns true if no edge from source->target AND target->source exists
     *
     * @return
     */
    protected boolean existsNoEdge(GraphModel graphModel, Node source, Node target) {
        return (graphModel.getGraph().getEdge(source, target) == null
                && graphModel.getGraph().getEdge(target, source) == null);
    }

    /**
     * Get all edges connecting the given node to its neighbors. Edge direction
     * is ignored.
     */
    protected List<Edge> getUndirectedEdges(GraphModel graphModel, Node node) {
        Edge edge = null;
        Graph graph = graphModel.getGraph();
        List<Edge> edges = new ArrayList<Edge>();

        Node[] neighbors = graph.getNeighbors(node).toArray();
        for (Node neighbor : neighbors) {
            if ((edge = graph.getEdge(node, neighbor)) != null) {
                edges.add(edge);
            } else if ((edge = graph.getEdge(neighbor, node)) != null) {
                edges.add(edge);
            }

        }

        return edges;
    }

    /**
     * Generate a random graph of given size, wiring with or without weighted
     * edges
     */
    protected void initializeRandomGraph(List<Node> nodeArray, Graph graph, int size, double pRandomWiring,
            boolean weighted, boolean powerDistributed) {
        Random random = new Random();
        GraphModel graphModel = graph.getGraphModel();

        // add initial two nodes
        for (int i = 0; i < size; ++i) {
            // create a node
            Node node = graphModel.factory().newNode();
            // initialize node
            node.getNodeData().setSize(NODE_SIZE);
            node.getNodeData().setLabel("" + (i + 1));
            // add to graph
            graph.addNode(node);
            nodeArray.add(node);
        }

        for (int i = 0; i < size - 1; i++) {
            Node node1 = nodeArray.get(i);
            for (int j = i + 1; j < size; j++) {
                Node node2 = nodeArray.get(j);
                if (random.nextDouble() < pRandomWiring) {
                    float weight = powerDistributed ? getPowerDistributedIntegerValue(random, 0, 1) : random.nextFloat();

                    Edge edge = graphModel.factory().newEdge(node1, node2);
                    if (weighted) {
                        edge.setWeight(weight);
                    }
                    graph.addEdge(edge);

                    edge = graphModel.factory().newEdge(node2, node1);
                    if (weighted) {
                        edge.setWeight(weight);
                    }
                    graph.addEdge(edge);
                }
            }
        }
    }

    /**
     * Implemented as defined by
     * http://en.wikipedia.org/wiki/Watts_and_Strogatz_Model#Algorithm
     */
    protected void createSmallWorldCommunity(Cell cell, GraphModel graphModel,
            Random random, int KMin, int KMax, double intraWiring,
            boolean isPrefferential, boolean clusterDistribution) {
        // N ~ 100 > K = [10-50] > ln(N) = 2 > 1

        // size if cluster
        int n = cell.getSize();
        // save edges in a local collection
        ArrayList<Edge> edges = new ArrayList<Edge>();

        // create ring
        for (int i = 0; i < n; i++) {
            Node node1 = cell.getNodes().get(i);

            // generate unique power-law K for each node
            int K;
            if (clusterDistribution) {
                K = getDistributedClusterSize(random, KMin, KMax > n ? n : KMax);
            } else {
                K = getPowerDistributedIntegerValue(random, KMin, KMax > n ? n : KMax);
            }

            int j = ((i - K) >= 0) ? (i - K) : (n - K);
            for (int count = 0; count < 2 * K + 1; count++) {
                Node node2 = cell.getNodes().get(j);
                if (node1 != node2) {
                    Edge edge = graphModel.factory().newEdge(node1, node2);
                    graphModel.getGraph().addEdge(edge);
                    edges.add(edge);

                    //Sleep some time
                    animateEdge();
                }
                j++;
                j %= n;
            }
        }

        // insert long range links into ring
        int error = 0;
        for (Edge edge : edges) {
            if (random.nextDouble() < intraWiring) {
                // keep source
                Node source = edge.getSource();
                // pick random target
                Node target = source;
                // pick prefferentially: highest degree
                if (isPrefferential) {
                    Node maxDegree = cell.getNodes().get(0);
                    for (Node node : cell.getNodes()) {
                        if (graphModel.getGraph().getDegree(node) > graphModel.getGraph().getDegree(maxDegree)
                                && !node.equals(source)) {
                            maxDegree = node;
                        }
                    }
                    target = maxDegree;
                } // pick random
                else {
                    while (target.equals(source)) {
                        target = cell.getNodes().get(random.nextInt(cell.getSize()));
                    }
                }

                try {
                    graphModel.getGraph().writeLock();
                    graphModel.getGraph().removeEdge(edge);
                } catch (NullPointerException np) {
                    /* ignore */
                    error++;
                } finally {
                    graphModel.getGraph().writeUnlock();
                }
                Edge newEdge = graphModel.factory().newEdge(source, target);
                graphModel.getGraph().addEdge(newEdge);

                //Sleep some time
                animateEdge();
            }
        }

        edges = null;
    }

    /**
     * Return a power law (one-sided Gaussian) distributed cluster size. <br>
     * Most occurrent (mean) is MIN size. <br> Least occurrent is MAX size. <br>
     *
     * @param random An initialized random generator used for all Gaussian
     * numbers.
     * @return a integer between [min, max] according to a power law
     * distribution.
     */
    protected int getDistributedClusterSize(Random random, int min, int max) {
        // Gaussian number between -3 and +3 with mean 0.
        double x = random.nextGaussian();

        // the closer to 0, the smaller the cluster
        // the further from 0, the larger the cluster
        double dx = Math.abs(x);
        if (dx > 3) {
            dx = 3;
        }
        dx /= 3.0;

        if (dx < 0.233) {
            return random.nextInt(8) + min;
        } else {
            return min + (int) ((max - min) * (dx));
        }
    }

    protected int getPowerDistributedIntegerValue(Random random, int min, int max) {
        // Gaussian number between -3 and +3 with mean 0.
        double x = random.nextGaussian();

        // the closer to 0, the smaller the cluster
        // the further from 0, the larger the cluster
        double dx = Math.abs(x);
        if (dx > 3) {
            dx = 3;
        }
        dx /= 3.0;

        return min + (int) ((max - min) * (dx));
    }

    protected double getPowerDistributedDoubleValue(Random random, int min, int max) {
        // Gaussian number between -3 and +3 with mean 0.
        double x = random.nextGaussian();

        // the closer to 0, the smaller the cluster
        // the further from 0, the larger the cluster
        double dx = Math.abs(x);
        if (dx > 3) {
            dx = 3;
        }
        dx /= 3.0;

        return min + (max - min) * dx;
    }

    /**
     * Return a normal-distributed (Gaussian) number. <br>
     *
     * @param random An initialized random generator used for all Gaussian
     * numbers.
     * @return a double centered around [mean]
     */
    protected float getNormalDistributedNumber(Random random, double mean) {
        double x = -1;

        while (x < 0 || x > 1) {
            // Gaussian number between -3 and +3 with mean 0.
            x = random.nextGaussian();
            // normalize to [-0.5 , +0.5]
            x /= 6;
            // center around mean
            x += mean;
        }
        return (float) x;
    }

    /**
     * Return an random object from the list that is different from the passed
     * argument. Additionally an element that is in the ă0, endIndex) interval
     * can be returned.
     *
     * @param list
     * @param differentThan
     * @param endIndex
     * @param random
     * @return
     */
    protected Object getRandomAndDifferent(Object[] list, Object differentThan, int endIndex, Random random) {
        Object obj = differentThan;

        if (list.length < 2 || endIndex < 2) {
            return null;
        }

        while (obj == null || obj.equals(differentThan)) {
            obj = list[random.nextInt(endIndex)];
        }

        return obj;
    }

    /**
     * Return a random integer index between start (include) and end (exclude) that is different than the given index
     * @param start
     * @param end
     * @param differentThan
     * @param random
     * @return 
     */
    protected int getRandomAndDifferentIndex(int start, int end, int differentThan, Random random) {
        int selected = differentThan;

        while (selected == differentThan) {
            selected = random.nextInt(end - start) + start;
        }

        return selected;
    }

    // <editor-fold defaultstate="collapsed" desc="Common Getters/Setters">
    public boolean getAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public int getAnimationNodeDelay() {
        return animationNodeDelay;
    }

    public void setAnimationNodeDelay(int animationNodeDelay) {
        this.animationNodeDelay = animationNodeDelay;
    }

    public int getAnimationEdgeDelay() {
        return animationEdgeDelay;
    }

    public void setAnimationEdgeDelay(int animationEdgeDelay) {
        this.animationEdgeDelay = animationEdgeDelay;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Utility">
    protected void sleep(int delay) {
        //Sleep some time
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void animateNode() {
        if (animate) {
            sleep(animationNodeDelay);
        }
    }

    protected void animateEdge() {
        if (animate) {
            sleep(animationEdgeDelay);
        }
    }

    protected void progressTick() {
        Progress.progress(progress, ++progressState);
    }
    // </editor-fold>
}
