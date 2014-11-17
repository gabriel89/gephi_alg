package org.gephi.statistics.plugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Compares two networks using the delta fidelity metric. <br> Uses basic
 * metrics as inputs: average D, L, C, modularity, density, diameter. <br>
 *
 * @author Alexander
 */
public class WSNOptimizer implements Statistics, LongTask {

    // number of syncs to be added
    private int nExtraEdges = 0;
    private int ndelegates = 8;
    private double Radius = 1000;
    private int wifiRadius = 20;
    private double resolution = 0.5;
    private double populationTreshold = 0.02;
    private boolean grow = false;
    private int ratio = 250;
    private boolean directed = false;
    private final float[] syncColor = {1f, 0f, 0f};
    private final float[] sensorColor = {0.4f, 0.4f, 0.4f};
    private final float[] wiEdgeColor = {1f, 1f, 0f};
    private final float[] phEdgeColor = {1f, 0f, 0f};
    private final int nodeSize = 5;
    private final int syncSize = 10;
    private final float wiEdgeSize = 1.0f;
    private final float phEdgeSize = 1.5f;
    private boolean animate = false;
    private int animationNodeDelay = 10;
    private int animationEdgeDelay = 50;
    /**
     * Remembers if the Cancel function has been called.
     */
    private boolean isCanceled;
    /**
     * Keep track of the work done.
     */
    private ProgressTicket progress;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public int getNExtraEdges() {
        return nExtraEdges;
    }

    public double getResolution() {
        return resolution;
    }

    public int getWifiRadius() {
        return wifiRadius;
    }

    public boolean getGrow() {
        return grow;
    }

    public int getRatio() {
        return ratio;
    }

    public boolean getDirected() {
        return directed;
    }

    public void setNExtraEdges(int nExtraEdges) {
        this.nExtraEdges = nExtraEdges;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public void setWifiRadius(int wifiRadius) {
        this.wifiRadius = wifiRadius;
    }

    public void setGrow(boolean grow) {
        this.grow = grow;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
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
    // <editor-fold defaultstate="collapsed" desc="Execution">
    private int numberOfSinks, numberofComm;
    private double avgDelay;

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {

        //graph.readLock();
        Progress.start(progress);
        progress.switchToIndeterminate();

        /**
         * 0. Add wifi edges if graph is empty
         */
        if (graph.getEdgeCount() == 0) {
            // for all node-pairs
            for (Node source : graph.getNodes().toArray()) {
                //source.getNodeData().setSize(3f);
                for (Node target : graph.getNodes().toArray()) {
                    if (!source.equals(target)) {

                        // compute distance
                        double dist = getDistance(source, target);

                        // add edge if inside wifi distance
                        if (dist <= wifiRadius) {
                            // if probability                            
                            createEdge(graph.getGraphModel(), source, target);
                        }
                    }
                }
            }
        }

        /*
         * 1. measure eigenvector centrality to determine super-sync
         */

        // compute centrality of nodes
        EigenvectorCentrality centrality = new EigenvectorCentrality();
        centrality.setDirected(directed);
        centrality.setNumRuns(100);
        centrality.setProgressTicket(progress);
        centrality.execute(graph.getGraphModel(), attributeModel);
        centrality.getNumRuns();

        // order in descending order of centrality
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (Node node : graph.getNodes()) {
            nodes.add(node);
        }
        ArrayList<Edge> edges = new ArrayList<Edge>();
        for (Edge edge : graph.getEdges()) {
            edges.add(edge);
        }

        Collections.sort(nodes, new NodeComparator(EigenvectorCentrality.EIGENVECTOR, false));

        // pick first node to be super-sync
        ArrayList<Node> syncs = new ArrayList<Node>();
        syncs.addAll(nodes.subList(0, 1));
        nodes.removeAll(syncs);

        // highlight syncs
        for (Node node : nodes) {
            node.getNodeData().setColor(sensorColor[0], sensorColor[1], sensorColor[2]);
            node.getNodeData().setSize(nodeSize);
        }

        /*
         * 2. Detect communities
         */

        // run modularity on network
        Modularity modularity = new Modularity();
        modularity.setUseWeight(false);
        modularity.setRandom(true);
        modularity.setResolution(2 * resolution);
        modularity.setProgressTicket(progress);
        modularity.execute(graph.getGraphModel(), attributeModel);
        modularity.getModularity();

        // keep most significant communities
        LinkedList<Modularity.Community> communities = modularity.getCommunities();
        numberofComm = modularity.getCommunities().size();
        // map: community ID, community size
        HashMap<Integer, Integer> comSize = new HashMap<Integer, Integer>();
        int totalComSize = nodes.size() - 1;
        for (Node node : nodes) {
            Integer comId = (Integer) node.getAttributes().getValue(Modularity.MODULARITY_CLASS);
            // update sizes map
            if (comSize.get(comId) == null) {
                comSize.put(comId, 1);
            } else {
                comSize.put(comId, (Integer) comSize.get(comId) + 1);
            }
        }

        // keep significant ones; >5% population             
        Iterator<Integer> itc = comSize.keySet().iterator();
        while (itc.hasNext()) {
            Integer key = itc.next();
            Integer size = comSize.get(key);
            if (size != null) {
                if (1.0 * size / totalComSize < populationTreshold) {
                    itc.remove();
                }
            }
        }

        /*
         * 3. Assign most central syncs to significant communities
         */

        // select one central sync per significant community
        boolean[] comHasSync = new boolean[communities.size()];
        // init: flag super-sync community
        comHasSync[(Integer) (syncs.get(0).getAttributes().getValue(Modularity.MODULARITY_CLASS))] = true;
        Iterator<Node> itn = nodes.iterator();
        while (itn.hasNext()) {
            Node node = itn.next();
            int comId = (Integer) node.getAttributes().getValue(Modularity.MODULARITY_CLASS);
            // flag community with assigned sync
            if (!comHasSync[comId] && comSize.keySet().contains(comId)) {
                comHasSync[comId] = true;
                syncs.add(node);
                itn.remove();
            }
        }

        // highlight syncs
        for (Node sync : syncs) {
            sync.getNodeData().setColor(syncColor[0], syncColor[1], syncColor[2]);
            sync.getNodeData().setSize(syncSize);
        }
        // highlight super-sync
        syncs.get(0).getNodeData().setSize(1.75f * syncSize);
        numberOfSinks = syncs.size();

        // compute average path length (average delay) to a sink
        GraphDistance distance = new GraphDistance();
        distance.setDirected(directed);
        distance.setProgressTicket(progress);
        distance.setNormalized(false);
        for (Node node : nodes) {
            double closest = Double.MAX_VALUE;
            for (Node sync : syncs) {
                double dist = getDistance(node, sync);
                closest = dist < closest ? dist : closest;
            }
            closest /= (1.0 * wifiRadius);
            closest = Math.ceil(closest);

            avgDelay += closest;
        }
        avgDelay /= (1.0 * nodes.size());

        /*
         * 4. Connect all syncs to super-sync
         */

        // color all wireless links (yellow)
        for (Edge edge : edges) {
            edge.getEdgeData().setColor(wiEdgeColor[0], wiEdgeColor[1], wiEdgeColor[2]);
            edge.getEdgeData().setSize(wiEdgeSize);
        }

        // minimum spanning tree for syncs
        Kruskal kruskal = new Kruskal();
        kruskal.createMST(syncs, graph.getGraphModel(), attributeModel);
        ArrayList<Edge> mstEdges = kruskal.getEdges();

        for (Edge edge : mstEdges) {
            Edge newEdge = createEdge(graph.getGraphModel(), edge.getSource(), edge.getTarget());

            // if edge alread yexisted, retrieve it
            if (newEdge == null) {
                newEdge = graph.getEdge(edge.getSource(), edge.getTarget());
                // search both ways
                if (newEdge == null) {
                    newEdge = graph.getEdge(edge.getTarget(), edge.getSource());
                }
            }

            // highlight physical edge
            newEdge.getEdgeData().setColor(phEdgeColor[0], phEdgeColor[1], phEdgeColor[2]);
            newEdge.getEdgeData().setSize(phEdgeSize);
        }

        mstEdges.clear();
        mstEdges = null;

        /*
         * 5. Growth simulation
         */

        if (grow) {

            // nodes to add
            int newSize = nodes.size() * ratio / 100;
            // new radius
            int newRadius = (int) (Radius * ratio / 100);
            Random random = new Random();

            // position inside circle
            ArrayList<Node> neighbors;
            for (int i = 0; i < newSize; ++i) {
                int x, y;

                boolean canConnect = false;
                neighbors = new ArrayList<Node>();

                // try to connect the node to the network
                while (!canConnect) {
                    // generate random x, y
                    x = random.nextInt(newRadius) - newRadius / 2;
                    y = random.nextInt(newRadius) - newRadius / 2;


                    for (Node node : nodes) {
                        if (getDistance(node, x, y) < wifiRadius) {
                            canConnect = true;
                            // add as neighbor
                            neighbors.add(node);
                        }
                    }

                    canConnect = neighbors.size() > 0 && neighbors.size() < 10;

                    if (canConnect) {
                        // create node
                        Node node = graph.getGraphModel().factory().newNode();
                        // initialize node
                        node.getNodeData().setSize(nodeSize);
                        node.getNodeData().setLabel("" + nodes.size());
                        // add to graph
                        graph.getGraphModel().getGraph().addNode(node);
                        nodes.add(node);
                        node.getNodeData().setX(x);
                        node.getNodeData().setY(y);

                        //Sleep some time
                        animateNode();

                        // draw edges to neighbors
                        for (Node neighbor : neighbors) {
                            Edge edge = createEdge(graph.getGraphModel(), node, neighbor);

                            // highlight physical edge
                            edge.getEdgeData().setColor(wiEdgeColor[0], wiEdgeColor[1], wiEdgeColor[2]);
                            edge.getEdgeData().setSize(wiEdgeSize);

                            animateEdge();
                        }
                    }
                    neighbors.clear();
                }
            }

            // recompute communities

            // compute centrality of nodes
            centrality = new EigenvectorCentrality();
            centrality.setDirected(directed);
            centrality.setNumRuns(100);
            centrality.setProgressTicket(progress);
            centrality.execute(graph.getGraphModel(), attributeModel);
            centrality.getNumRuns();

            Collections.sort(nodes, new NodeComparator(EigenvectorCentrality.EIGENVECTOR, false));

            // run modularity on network
            modularity = new Modularity();
            modularity.setUseWeight(false);
            modularity.setRandom(true);
            modularity.setResolution(2 * resolution);
            modularity.setProgressTicket(progress);
            modularity.execute(graph.getGraphModel(), attributeModel);
            modularity.getModularity();

            // keep most significant communities
            communities.clear();
            communities = modularity.getCommunities();
            // map: community ID, community size
            comSize.clear();
            comSize = new HashMap<Integer, Integer>();
            totalComSize = nodes.size() - 1;
            for (Node node : nodes) {
                Integer comId = (Integer) node.getAttributes().getValue(Modularity.MODULARITY_CLASS);
                // update sizes map
                if (comSize.get(comId) == null) {
                    comSize.put(comId, 1);
                } else {
                    comSize.put(comId, (Integer) comSize.get(comId) + 1);
                }
            }

            // keep significant ones; >5% population                         
            itc = comSize.keySet().iterator();
            while (itc.hasNext()) {
                Integer key = itc.next();
                Integer size = comSize.get(key);
                if (size != null) {
                    if (1.0 * size / totalComSize < populationTreshold) {
                        itc.remove();
                    }
                }
            }

            // select one central sync per significant community
            comHasSync = new boolean[communities.size()];
            // init: flag super-sync community
            comHasSync[(Integer) (syncs.get(0).getAttributes().getValue(Modularity.MODULARITY_CLASS))] = true;
            itn = nodes.iterator();
            while (itn.hasNext()) {
                Node node = itn.next();
                int comId = (Integer) node.getAttributes().getValue(Modularity.MODULARITY_CLASS);
                // flag community with assigned sync
                if (!comHasSync[comId] && comSize.keySet().contains(comId)) {
                    comHasSync[comId] = true;
                    syncs.add(node);
                    itn.remove();
                }
            }

            // highlight syncs
            for (Node sync : syncs) {
                sync.getNodeData().setColor(syncColor[0], syncColor[1], syncColor[2]);
                sync.getNodeData().setSize(syncSize);
            }

            // highlight super-sync
            syncs.get(0).getNodeData().setSize(1.75f * syncSize);

            // color all wireless links (yellow)
            for (Edge edge : edges) {
                edge.getEdgeData().setColor(wiEdgeColor[0], wiEdgeColor[1], wiEdgeColor[2]);
                edge.getEdgeData().setSize(wiEdgeSize);
            }

            // minimum spanning tree for syncs
            kruskal = new Kruskal();
            kruskal.createMST(syncs, graph.getGraphModel(), attributeModel);
            mstEdges = kruskal.getEdges();

            for (Edge edge : mstEdges) {
                Edge newEdge = createEdge(graph.getGraphModel(), edge.getSource(), edge.getTarget());

                // if edge alread yexisted, retrieve it
                if (newEdge == null) {
                    newEdge = graph.getEdge(edge.getSource(), edge.getTarget());
                    // search both ways
                    if (newEdge == null) {
                        newEdge = graph.getEdge(edge.getTarget(), edge.getSource());
                    }
                }

                // highlight physical edge
                newEdge.getEdgeData().setColor(phEdgeColor[0], phEdgeColor[1], phEdgeColor[2]);
                newEdge.getEdgeData().setSize(phEdgeSize);
            }

            // discard MST edges
            mstEdges.clear();
            mstEdges = null;
        }

        syncs.clear();
        syncs = null;
        nodes.clear();
        nodes = null;
        edges.clear();
        edges = null;

        progress.switchToDeterminate(100);
        progress.finish();
        //graph.readUnlockAll();
    }

    // <editor-fold defaultstate="collapsed" desc="Old Execution">
    public void executee(HierarchicalGraph graph, AttributeModel attributeModel) {

        //graph.readLock();
        Progress.start(progress);
        progress.switchToIndeterminate();

        /*
         * 1. measure betweenness / degree of nodes
         */

        // compute degree of nodes
        Degree degree = new Degree();
        degree.setProgressTicket(progress);
        degree.execute(graph.getGraphModel(), attributeModel);
        degree.getAverageDegree();

        // compute betweenness of nodes
        GraphDistance betweenness = new GraphDistance();
        betweenness.setNormalized(false);
        betweenness.setDirected(directed);
        betweenness.setProgressTicket(progress);
        betweenness.execute(graph.getGraphModel(), attributeModel);
        betweenness.getPathLength();

        // order in descending order of betweenness / degree
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (Node node : graph.getNodes()) {
            nodes.add(node);
        }
        ArrayList<Edge> edges = new ArrayList<Edge>();
        for (Edge edge : graph.getEdges()) {
            edges.add(edge);
        }

        //Collections.sort(nodes, new NodeComparator(GraphDistance.BETWEENNESS, false));
        Collections.sort(nodes, new NodeComparator(Degree.DEGREE, false));

        // pick first 'nsyncs' nodes to be syncs
        ArrayList<Node> syncs = new ArrayList<Node>();
        syncs.addAll(nodes.subList(0, 1));
        nodes.removeAll(syncs);

        // highlight syncs
        for (Node node : nodes) {
            node.getNodeData().setColor(sensorColor[0], sensorColor[1], sensorColor[2]);
            node.getNodeData().setSize(nodeSize);
        }

        for (Node sync : syncs) {
            sync.getNodeData().setSize(sync.getNodeData().getSize() * 3);
            sync.getNodeData().setColor(syncColor[0], syncColor[1], syncColor[2]);
            sync.getNodeData().setSize(syncSize);
        }

        /*
         * 2. Create physical network of syncs (and some senors...)
         */

        // color all wireless links yellow
        for (Edge edge : edges) {
            edge.getEdgeData().setColor(wiEdgeColor[0], wiEdgeColor[1], wiEdgeColor[2]);
            edge.getEdgeData().setSize(wiEdgeSize);
        }

        // minimum spanning tree for syncs
        Kruskal kruskal = new Kruskal();
        kruskal.createMST(syncs, graph.getGraphModel(), attributeModel);
        ArrayList<Edge> mstEdges = kruskal.getEdges();

        for (Edge edge : mstEdges) {
            Edge newEdge = createEdge(graph.getGraphModel(), edge.getSource(), edge.getTarget());

            // if edge alread yexisted, retrieve it
            if (newEdge == null) {
                newEdge = graph.getEdge(edge.getSource(), edge.getTarget());
                // search both ways
                if (newEdge == null) {
                    newEdge = graph.getEdge(edge.getTarget(), edge.getSource());
                }
            }

            // highlight physical edge
            newEdge.getEdgeData().setColor(phEdgeColor[0], phEdgeColor[1], phEdgeColor[2]);
            newEdge.getEdgeData().setSize(phEdgeSize);
        }

        mstEdges.clear();
        mstEdges = null;

        // apply BA scale free algorithm
        // choose leaders=syncs and delegates=out of range sensors        
        ArrayList<Node> sfNodes = new ArrayList<Node>();
        Random random = new Random();

        // add delegates
        sfNodes.addAll(syncs);
        // for each leader
        for (int i = 0; i < syncs.size(); ++i) {

            Node leader = syncs.get(i);
            Node delegate = null;
            int k = ndelegates;

            // sort remaining nodes in descending order of betweenness / distance(leader)^pow
            //Collections.sort(nodes, new NodeComparator(GraphDistance.BETWEENNESS, false, leader));            
            Collections.sort(nodes, new NodeComparator(Degree.DEGREE, false, leader));

            while (k > 0) {
                // compute sum of all betweennesses in network weighted with the distances to the current leader
                double sum = 0;
                for (int j = 0; j < nodes.size(); ++j) {
                    sum += (Double) (nodes.get(j).getAttributes().getValue(NodeComparator.FITNESS));
                }

                double p = random.nextDouble();

                // iterate nodes in descending order of betweenness / distance
                int j = 0;
                while (p > 0) {
                    p -= (Double) (nodes.get(j).getAttributes().getValue(NodeComparator.FITNESS)) / sum;
                    j++;
                }

                // node 'j-1' has been selected as delegate     
                delegate = nodes.get(j - 1);
                // try to connect the node to the current leader
                // try to create an edge between leader and delegate 
                Edge edge = createEdge(graph.getGraphModel(), leader, delegate);
                // if edge alread yexisted, retrieve it
                if (edge == null) {
                    edge = graph.getEdge(leader, delegate);
                    // search both ways
                    if (edge == null) {
                        edge = graph.getEdge(delegate, leader);
                    }
                }

                // highlight physical edge
                edge.getEdgeData().setColor(phEdgeColor[0], phEdgeColor[1], phEdgeColor[2]);
                edge.getEdgeData().setSize(phEdgeSize);

                // save delegate
                if (!sfNodes.contains(delegate)) {
                    sfNodes.add(delegate);
                    nodes.remove(delegate);
                }

                animateEdge();
                k--;
            }
        }

        // 3. Growth simulation

        if (grow) {

            // nodes to add
            int newSize = nodes.size() * ratio;
            // new radius
            double newRadius = Radius * ratio / 100.0;

            // add nodes using sclae free coordinates            
        }

        syncs.clear();
        syncs = null;
        sfNodes.clear();
        sfNodes = null;
        nodes.clear();
        nodes = null;
        edges.clear();
        edges = null;

        progress.switchToDeterminate(100);
        progress.finish();
        //graph.readUnlockAll();
    }
    // </editor-fold>
// </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Misc Area">
    private String errorReport = "";

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

    /*
     * Get euclidean distance between a node and a pair of coordinates
     */
    private double getDistance(Node n1, int x, int y) {

        double x1 = n1.getNodeData().x();
        double y1 = n1.getNodeData().y();
        double dist = Math.sqrt((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y));

        return dist;
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

        String report = "<HTML> <BODY> <h1>WSN Optimizer Report </h1> "
                + "<hr><br>";

        report += "Sinks assigned: " + numberOfSinks + "<br>";
        report += "Number of communities: " + numberofComm + "<br>";
        report += "Average delay (hops): " + avgDelay + "<br>";

        /*report += "<table border=\"1\"><tr><th></th>";
         report += "<th>ADeg</th>"
         + "<th>APL</th>"
         + "<th>CC</th>"
         + "<th>Mod</th>"
         + "<th>Dns</th>"
         + "<th>Dmt</th>"
         + "</tr>";

         report += "<tr><td><b>Base model</b></td>";
         for (Double value : baseMetrics) {
         report += "<td>" + f.format(value) + "</td>";
         }

         report += "</tr><tr><td><b>Measured model</b></td>";
         for (Double value : measuredMetrics) {
         report += "<td>" + f.format(value) + "</td>";
         }

         report += "</tr><tr><td><b>Individual deltas</b></td>";
         for (int i = 0; i < measuredMetrics.length; ++i) {
         report += "<td>" + f.format(getDelta(measuredMetrics[i], i)) + "</td>";
         }

         report += "</tr></table><br><br>";

         report += "Arithmetic Delta: " + f.format(getDeltaArithmetic()) + "<br>";
         report += "Geometric Delta: " + f.format(getDeltaGeometric()) + "<br>";
         report += "Harmonic Delta: " + f.format(getDeltaHarmonic()) + "<br>";*/

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

    private int getPowerLawValue(Random random, int min, int max) {
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
    // </editor-fold>

    public class NodeComparator implements Comparator<Node> {

        public static final String FITNESS = "Fitness";
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

            Object o1 = node1.getAttributes().getValue(toCompare);
            Object o2 = node2.getAttributes().getValue(toCompare);

            if (o1 == null || o2 == null) {
                return 0;
            }

            Double f1, f2;

            if (o1 instanceof Integer) {
                f1 = (double) ((Integer) o1).intValue();
                f2 = (double) ((Integer) o2).intValue();
            } else if (o1 instanceof Double) {
                f1 = (Double) o1;
                f2 = (Double) o2;
            } else {
                throw new NumberFormatException("Data is not integer or double");
            }

            if (reference != null) {
                f1 /= (1.0 * Math.pow(getDistance(reference, node1), 3));
                f2 /= (1.0 * Math.pow(getDistance(reference, node2), 3));

                node1.getAttributes().setValue(FITNESS, f1);
                node2.getAttributes().setValue(FITNESS, f2);
            }

            // compare a 'statistic' of each node
            if (ascending) {
                return f1.compareTo(f2);
            } else {
                return f2.compareTo(f1);
            }
        }
    }

    public class EdgeComparator implements Comparator<Edge> {

        private String toCompare;
        private boolean ascending;

        public EdgeComparator(String toCompare, boolean ascending) {
            this.toCompare = toCompare;
            this.ascending = ascending;
        }

        public int compare(Edge edge1, Edge edge2) {

            Double f1 = (Double) edge1.getAttributes().getValue(toCompare);
            Double f2 = (Double) edge2.getAttributes().getValue(toCompare);

            if (f1 == null || f2 == null) {
                return 0;
            }

            // compare a 'statistic' of each edge
            if (ascending) {
                return f1.compareTo(f2);
            } else {
                return f2.compareTo(f1);
            }
        }
    }

    public class Kruskal {

        public static final String DISTANCE = "Distance";
        public static final String VISITED = "Visited";
        public static final String EDGE_FITNESS = "Fitness";
        private ArrayList<HashSet<Node>> vertexGroups = new ArrayList<HashSet<Node>>();
        private ArrayList<Edge> mstEdges = new ArrayList<Edge>();

        public ArrayList<Edge> getEdges() {
            return mstEdges;
        }

        public HashSet<Node> getVertexGroup(Node vertex) {
            for (HashSet<Node> vertexGroup : vertexGroups) {
                if (vertexGroup.contains(vertex)) {
                    return vertexGroup;
                }
            }
            return null;
        }

        public boolean insertEdge(Edge edge) {
            Node vertexA = edge.getSource();
            Node vertexB = edge.getTarget();

            HashSet<Node> vertexGroupA = getVertexGroup(vertexA);
            HashSet<Node> vertexGroupB = getVertexGroup(vertexB);

            if (vertexGroupA == null) {
                mstEdges.add(edge);
                if (vertexGroupB == null) {
                    HashSet<Node> htNewVertexGroup = new HashSet<Node>();
                    htNewVertexGroup.add(vertexA);
                    htNewVertexGroup.add(vertexB);
                    vertexGroups.add(htNewVertexGroup);
                } else {
                    vertexGroupB.add(vertexA);
                }
                return true;
            } else {
                if (vertexGroupB == null) {
                    vertexGroupA.add(vertexB);
                    mstEdges.add(edge);
                    return true;
                } else if (vertexGroupA != vertexGroupB) {
                    vertexGroupA.addAll(vertexGroupB);
                    vertexGroups.remove(vertexGroupB);
                    mstEdges.add(edge);
                    return true;
                }
            }
            // edge not added
            return false;
        }

        /**
         * Create minimum coverage tree over the given list of geo-located
         * vertices using Kruskal's algorithm
         *
         * @param nodes
         * @return
         */
        public void createMST(ArrayList<Node> nodes, GraphModel graphModel, AttributeModel attributeModel) {

            mstEdges.clear();
            ArrayList<Edge> allEdges = new ArrayList<Edge>();

            // add initial point to point edges
            for (int i = 0; i < nodes.size() - 1; ++i) {
                for (int j = i + 1; j < nodes.size(); ++j) {
                    Edge edge = graphModel.factory().newEdge(nodes.get(i), nodes.get(j));
                    edge.getAttributes().setValue(DISTANCE, getDistance(edge.getSource(), edge.getTarget()));
                    allEdges.add(edge);
                }
            }

            // sort edges in ascending order of distance (cost)
            Collections.sort(allEdges, new EdgeComparator(DISTANCE, true));

            // insert edges according to Kruskal
            Iterator<Edge> it = allEdges.iterator();
            while (it.hasNext()) {
                Edge edge = it.next();
                if (insertEdge(edge)) {
                    it.remove();
                }
            }

            /**
             * Add some more edges to rise centrality
             */
            // create new local graph
            Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
            Dhns dhns = new DhnsGraphController().newDhns(currentWorkspace);
            HierarchicalDirectedGraph sGraph = dhns.getHierarchicalDirectedGraph();

            ArrayList<Node> sNodes = new ArrayList<Node>();
            ArrayList<Edge> sEdges = new ArrayList<Edge>();

            // initialize temporary graph: clone nodes and edges
            HashMap<Integer, Integer> idBaseToLocal = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> idLocalToBase = new HashMap<Integer, Integer>();
            //HashMap<Integer, Integer> ids = new HashMap<Integer, Integer>();
            for (int i = 0; i < nodes.size(); ++i) {
                Node newNode = sGraph.getGraphModel().factory().newNode();
                idBaseToLocal.put(nodes.get(i).getId(), (i + 1));
                idLocalToBase.put((i + 1), nodes.get(i).getId());
                sGraph.addNode(newNode);
                sNodes.add(newNode);
            }
            for (Edge edge : mstEdges) {
                Integer id1 = idBaseToLocal.get(edge.getSource().getId());
                Integer id2 = idBaseToLocal.get(edge.getTarget().getId());
                Edge newEdge = sGraph.getGraphModel().factory().newEdge(
                        sGraph.getNode(id1), sGraph.getNode(id2));
                sGraph.addEdge(newEdge);
                //sEdges.add(newEdge);
            }

            // initial fitness           
            EigenvectorCentrality centrality = new EigenvectorCentrality();
            centrality.setDirected(false);
            centrality.setNumRuns(100);
            centrality.setProgressTicket(progress);
            centrality.execute(sGraph.getGraphModel(), attributeModel);
            centrality.getNumRuns();
            Double fit0 = (Double) sNodes.get(0).getAttributes().getValue(EigenvectorCentrality.EIGENVECTOR);

            // add best edge               
            // measure fitness for all other edges            
            for (Edge edge : allEdges) {

                // test with this edge
                Integer id1 = idBaseToLocal.get(edge.getSource().getId());
                Integer id2 = idBaseToLocal.get(edge.getTarget().getId());
                Edge newEdge = sGraph.getGraphModel().factory().newEdge(
                        sGraph.getNode(id1), sGraph.getNode(id2));
                sGraph.addEdge(newEdge);
                sEdges.add(newEdge);

                // run centrality
                centrality.execute(sGraph.getGraphModel(), attributeModel);
                centrality.getNumRuns();

                // measure new fitness of super-sync
                Double fit = (Double) sNodes.get(0).getAttributes().getValue(EigenvectorCentrality.EIGENVECTOR);
                newEdge.getAttributes().setValue(EDGE_FITNESS, fit);

                // reset graph by removing edge
                sGraph.removeEdge(newEdge);
            }

            // sort by fitness
            Collections.sort(sEdges, new EdgeComparator(EDGE_FITNESS, false));

            // add first 3 edges
            for (int i = 0; i < nExtraEdges; ++i) {
                // aaa
                Integer id1 = idLocalToBase.get(sEdges.get(i).getSource().getId());
                Integer id2 = idLocalToBase.get(sEdges.get(i).getTarget().getId());

                Edge newEdge = createEdge(graphModel, graphModel.getGraph().getNode(id1),
                        graphModel.getGraph().getNode(id2));

                if (newEdge == null) {
                    newEdge = graphModel.getGraph().getEdge(graphModel.getGraph().getNode(id1),
                            graphModel.getGraph().getNode(id2));
                    // search both ways
                    if (newEdge == null) {
                        newEdge = graphModel.getGraph().getEdge(graphModel.getGraph().getNode(id2),
                                graphModel.getGraph().getNode(id1));
                    }
                }
                mstEdges.add(newEdge);
            }


            sNodes.clear();
            sEdges.clear();;
            allEdges.clear();
        }
    }
}
