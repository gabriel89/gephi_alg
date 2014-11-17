package org.gephi.statistics.plugin.social;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

/**
 * Grows an existing social network using the TopiBara algorithm... <br>
 * Albert-Barabasi growth inspired by WIW network (Szendroi et al.)
 *
 * @author Alexander
 */
public class TopiBaraGraph implements Statistics, LongTask {

    // algorithm settings
    private int maxSize = 1000;
    private double growthFactor = 1.0;
    private double avgDegree = 12;
    // animation
    private boolean animate = false;
    private int animationNodeDelay = 10;
    private int animationEdgeDelay = 50;
    private boolean isCanceled;
    private ProgressTicket progress;
    // other
    protected static final float NODE_SIZE = 5f;

    // <editor-fold defaultstate="collapsed" desc="Execution">    
    private int _sizeInitial, _sizeFinal;
    private double _avgDegree;

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {

        //graph.readLock();
        Progress.start(progress);
        progress.switchToIndeterminate();

        Random random = new Random();
        _sizeInitial = graph.getNodeCount();

        /**
         * 0. Verify if an underlying graph already exists
         */
        // create two initial linked nodes
        if (graph.getEdgeCount() == 0 && graph.getNodeCount() == 0) {

            Node n0 = createNode(graph.getGraphModel());
            Node n1 = createNode(graph.getGraphModel());

            createEdge(graph.getGraphModel(), n0, n1);
        }

        /**
         * Loop algorithm until desired network size is achieved
         */
        while (graph.getNodeCount() < maxSize && !isCanceled) {

            /*
             * 1. Add new node(s)
             */

            // compute sum of all degrees in network
            int sumpk = 0;
            for (Node node : graph.getNodes()) {
                sumpk += graph.getDegree(node);
            }
            int oldNodeCount = graph.getNodeCount();
           
            // For every node invite new node with probability gamma*pk/sumpk
            for (Node node : graph.getNodes().toArray()) {

                double x = random.nextDouble();

                if (x <= 1.0 * growthFactor * graph.getDegree(node) / sumpk) {

                    // connect new node to inviting node
                    Node newNode = createNode(graph.getGraphModel());
                    createEdge(graph.getGraphModel(), node, newNode);
                }
            }
            
            /*Node newNode = createNode(graph.getGraphModel());
            boolean successs = false;
            while (!successs) {

                // try to connect the new node to nodes in the network
                for (Node node : graph.getNodes().toArray()) {
                    // degree of current sfNode
                    int pi = graph.getDegree(node);

                    // get random value
                    double p = random.nextDouble();

                    // connect if p < probability
                    if (p < 1.0 * pi / sumpk) {
                        createEdge(graph.getGraphModel(), node, newNode);
                        createEdge(graph.getGraphModel(), newNode, node);
                    }
                }
                
                successs = (graph.getDegree(newNode) > 0);
            }*/

            /*
             * 2. Add new edges
             */

            int newEdges = (int) (1.0 * graph.getEdgeCount() * (1.0 * graph.getNodeCount() / oldNodeCount - 1.0));
            //newEdges /= 2;
            
            boolean success = false;           
            for (int i = 0; i < newEdges; ++i) {
                // retry until an edge is added
                success = false;
                while (!success) {
                    // pick a random node
                    Node commonFriend = graph.getNodes().toArray()[random.nextInt(graph.getNodeCount())];

                    // randomly pick two of his unacquainted friend and connect them
                    Node[] friends = graph.getNeighbors(commonFriend).toArray();
                    // not enough friends!
                    if (friends.length <= 1) {
                        continue;
                    }

                    // choose two distinct friends
                    Node friend1 = friends[random.nextInt(friends.length)];
                    Node friend2 = friend1;

                    while (friend2.equals(friend1)) {
                        friend2 = friends[random.nextInt(friends.length)];
                    }

                    // check if connected and add edge
                    success = createEdge(graph.getGraphModel(), friend1, friend2) != null;             
                }
            }       

        }

        /*
         * 3. Complete number of edges
         */

        // number of new edges = up to required average degree
        int newEdges = (int) (1.0 * graph.getNodeCount() * (1.0 * avgDegree - graph.getEdgeCount() / graph.getNodeCount()));
        newEdges = 0;

        boolean success = false;
        for (int i = 0; i < newEdges; ++i) {
            // retry until an edge is added
            success = false;
            while (!success) {
                // pick a random node
                Node commonFriend = graph.getNodes().toArray()[random.nextInt(graph.getNodeCount())];

                // randomly pick two of his unacquainted friend and connect them
                Node[] friends = graph.getNeighbors(commonFriend).toArray();
                // not enough friends!
                if (friends.length <= 1) {
                    continue;
                }

                // choose two distinct friends
                Node friend1 = friends[random.nextInt(friends.length)];
                Node friend2 = friend1;

                while (friend2.equals(friend1)) {
                    friend2 = friends[random.nextInt(friends.length)];
                }

                // check if connected and add edge
                success = createEdge(graph.getGraphModel(), friend1, friend2) != null;
            }
        }

        _sizeFinal = graph.getNodeCount();
        _avgDegree = graph.getEdgeCount() / graph.getNodeCount();

        progress.switchToDeterminate(100);
        progress.finish();
        //graph.readUnlockAll();
    }
// </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public int getMaxSize() {
        return maxSize;
    }

    public double getGrowthFactor() {
        return growthFactor;
    }

    public double getAvgDegree() {
        return avgDegree;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void setGrowthFactor(double growthFactor) {
        this.growthFactor = growthFactor;
    }

    public void setAvgDegree(double avgDegree) {
        this.avgDegree = avgDegree;
    }

    // </editor-fold> 
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
    // </editor-fold>     
    // <editor-fold defaultstate="collapsed" desc="Misc Area">
    private String errorReport = "";

    /**
     * Add a new node to the social network.
     *
     * @param graphModel
     * @return
     */
    private Node createNode(GraphModel graphModel) {
        // create node
        Node node = graphModel.factory().newNode();
        // initialize nodes
        node.getNodeData().setSize(NODE_SIZE);
        node.getNodeData().setLabel(String.valueOf(graphModel.getGraph().getNodeCount() + 1));
        graphModel.getGraph().addNode(node);

        //Sleep some time
        animateNode();

        return node;
    }

    /**
     * Creates and edge between the nodes if none (undirected) exists
     */
    private Edge createEdge(GraphModel graphModel, Node n1, Node n2) {
        // create edge if none exists
        if (graphModel.getGraph().getEdge(n1, n2) == null
                && graphModel.getGraph().getEdge(n2, n1) == null) {

            Edge edge = graphModel.factory().newEdge(n1, n2);
            graphModel.getGraph().addEdge(edge);

            //Sleep some time
            animateEdge();

            return edge;
        }

        return null;
    }

    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.0000");

        String report = "<HTML> <BODY> <h1>TopiBara Graph Growth Report </h1> "
                + "<hr><br>";

        report += "Initial size : " + _sizeInitial + "<br>";
        report += "Final size: " + _sizeFinal + "<br>";
        report += "Average degree: " + _avgDegree + "<br>";

        report += "<br><br><font color=\"red\">" + errorReport + "</font>"
                + "</BODY></HTML>";

        return report;
    }

    /**
     *
     * @return
     */
    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    private boolean isCanceled() {
        return isCanceled;
    }

    /**
     *
     * @param progressTicket
     */
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
    // </editor-fold>     
}
