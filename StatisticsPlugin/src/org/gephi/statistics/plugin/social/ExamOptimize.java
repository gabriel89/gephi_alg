package org.gephi.statistics.plugin.social;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.SNode;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class ExamOptimize implements Statistics, LongTask {

    // size of the exam room
    private int w, h;
    // number of students
    private int n;
    // metric for best solution
    private double efficiency;
    // number of iterations to find best solution
    private int K = 3;
    // number of solutions in the genetic solution space
    private int NS = 30;
    // genetic properties
    private double PGen_Best = 0.4;
    private double PGen_Cross = 0.3;
    // flag set tot find highest values for efficiency (worst solutions) or lowest values (best solutions) 
    // default : false
    private boolean HIGH = false;
    private boolean isCanceled;
    private ProgressTicket progress;
    private int progressTick;
    // PERSISTENCY
    // saves the original social network
    private HierarchicalGraph originalGraph = null;
    // links statuses to the 8 neighbors
    private boolean[] neighborLinks = new boolean[9];

    // <editor-fold defaultstate="collapsed" desc="Execution">
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();

        // save to persistency if not initialized or changed
        if (SavedState.originalGraph == null || graph.hashCode() != SavedState.originalGraph.hashCode()) {
            SavedState.originalGraph = graph;
        }
        // restore persistent data
        this.originalGraph = SavedState.originalGraph;
        this.neighborLinks = SavedState.neighborLinks;

        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph ignoreThisGraph, AttributeModel attributeModel) {

        // create new local graph
        Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        Dhns dhns = new DhnsGraphController().newDhns(currentWorkspace);
        HierarchicalDirectedGraph graph = dhns.getHierarchicalDirectedGraph();

        Random rand = new Random();
        Node[][] students = new Node[h][w];
        n = originalGraph.getNodeCount();

        progressTick = 0;
        isCanceled = false;
        Progress.start(progress, 2 * NS * K);

        // create the nodes and mesh topology
        initializeMeshTopology(graph, students, w, h);

        //dbg
        PrintWriter pw0 = null;

        try {
            File tmp0 = new File(System.getProperty("user.home") + "/Desktop/lowest_sum.txt");
            pw0 = new PrintWriter(tmp0);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Genetic-stuff                        
        ArrayList<GeneticSolution> solutions = new ArrayList<GeneticSolution>(NS);

        // initialize NS random solutions in the solution space
        for (int k = 0; k < NS; ++k) {

            // map original nodes to mesh
            randomizeNodePlacement(originalGraph, graph, students, rand);

            // create solution and store it in solution space
            solutions.add(new GeneticSolution(originalGraph, students, w, h, n));
        }

        // K runs to find best solution
        for (int k = 0; k < K && !isCanceled; ++k) {
            efficiency = HIGH ? 0 : Double.MAX_VALUE;

            // step 1: measure all solutions
            for (GeneticSolution solution : solutions) {
                solution.computeEfficiency(graph);

                efficiency = HIGH ? 
                        (efficiency < solution.getScore() ? solution.getScore() : efficiency) :
                        (efficiency > solution.getScore() ? solution.getScore() : efficiency);
                //pw0.println("Sum:" + solution.getScore());

                Progress.progress(progress, ++progressTick);
                if (isCanceled) {
                    return;
                }
            }

            // step 2:  order solutions in ascending order of score (lower is better)
            pw0.println("** Random : " + solutions.get(rand.nextInt(solutions.size())).getScore() + " **");
            Collections.sort(solutions, new SolutionComparator(!HIGH));

            //dbg
            for (GeneticSolution solution : solutions) {
                pw0.println(k + ". sum:" + solution.getScore());
            }
            pw0.println(k + ". Lowest sum:" + efficiency);
            pw0.println();

            // step 3: play with the generation
            ArrayList<GeneticSolution> solutions2 = new ArrayList<GeneticSolution>(NS);
            int sBest = (int) (NS * PGen_Best);
            int sCross = (int) (NS * PGen_Cross);
            int i = 0;

            // 3.a: choose first % of best solutions
            for (; i < sBest; ++i) {
                solutions2.add(solutions.get(i));
            }

            // 3.b: choose a % of crossover solutions from the best solutions
            for (; i < sBest + sCross; ++i) {
                // get a random (best) solutions
                GeneticSolution gs = (GeneticSolution) getRandomAndDifferent(solutions2.toArray(), null, sBest, rand);

                // compute crossover treshold
                int crossover = rand.nextInt(n);

                // swap position of [0, crossover) with [crossover, n)
                // i.e. [a,b,c | d,e] -> [d,e,a,b,c]
                Node[] _nodes = graph.getNodes().toArray();
                Node[] _nodes2 = new Node[n];

                for (int c = crossover; c < n; ++c) {
                    _nodes2[c - crossover] = _nodes[c];
                }
                for (int c = 0; c < crossover; ++c) {
                    _nodes2[c + n - crossover] = _nodes[c];
                }

                // copy labels over original                
                int c = 0;
                for (Node n : graph.getNodes()) {
                    n.getNodeData().setLabel(_nodes2[c].getNodeData().getLabel());
                    c++;
                }

                solutions2.add(gs);
                Progress.progress(progress, ++progressTick);

                // clean
                _nodes = null;
                _nodes2 = null;
            }

            // 3.c. a mutated solutions from the full solution space
            for (; i < NS; ++i) {
                // get any random solution
                GeneticSolution gs = solutions.get(rand.nextInt(NS));

                // get two random nodes
                Node[] _nodes = graph.getNodes().toArray();
                Node n1 = (Node) getRandomAndDifferent(_nodes, null, n, rand);
                Node n2 = (Node) getRandomAndDifferent(_nodes, n1, n, rand);

                // swap the two nodes (their labels)
                String label = n1.getNodeData().getLabel();
                n1.getNodeData().setLabel(n2.getNodeData().getLabel());
                n2.getNodeData().setLabel(label);

                solutions2.add(gs);
                Progress.progress(progress, ++progressTick);

                // clean
                _nodes = null;
            }


            // copy & clean-up
            solutions.clear();
            solutions.addAll(solutions2);
            solutions2.clear();
            solutions2 = null;

            if (isCanceled) {
                return;
            }
        }


        pw0.println("Lowest sum:" + efficiency);        
        pw0.close();

        Progress.finish(progress);
        progress = null;

        // apply best solution onto current graph        
        solutions.get(0).applySolution(graph, students);

        // run layout        
        // close project and reopen new one                        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        if (projectController.getCurrentProject() != null) {
            projectController.closeCurrentProject();
        }
        projectController.newProject();

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        graphModel.pushFrom(graph);

        runGridLayout(graphModel.getHierarchicalGraph(), w, h);

        // clean-up
        for (GeneticSolution solution : solutions) {
            solution.clean();
            solution = null;
        }

    }
// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">

    public int getWidth() {
        return SavedState.w;
        //return w;        
    }

    public int getHeight() {
        return SavedState.h;
        //return h;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public boolean[] getNeighborLinks() {
        return SavedState.neighborLinks;
    }

    public int getN() {
        return n;
    }

    public int getK() {
        return K;
    }

    public void setWidth(int w) {
        this.w = w;
        SavedState.w = w;
    }

    public void setHeight(int h) {
        this.h = h;
        SavedState.h = h;
    }

    public void setK(int K) {
        this.K = K;
    }

    public void setNeighborLinks(boolean[] neighborLinks) {
        SavedState.neighborLinks = neighborLinks;
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Misc Area">
    /**
     * Search an ode in the graph by its label
     *
     * @return the found node or null
     */
    private Node getNodeByLabel(HierarchicalGraph graph, String label) {
        for (Node node : graph.getNodes()) {
            if (node.getNodeData().getLabel().equals(label)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Looks up a metric (by tag) in the original node and copies it to the
     * local node
     */
    private void addMetric(Node originalNode, Node localNode, String metricTag) {
        // lookup if metric exists in original graph
        if (originalNode.getAttributes().getValue(metricTag) != null) {
            // copy it to local node with "original_" prefix
            localNode.getAttributes().setValue("original_" + metricTag,
                    originalNode.getAttributes().getValue(metricTag));
        }
    }
    private String errorReport = "";

    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.00");

        String report = "<HTML> <BODY> <h1>Opinion Report </h1> "
                + "<hr>"
                + "<br> <h2> Results: </h2>"
                + "Efficiency : " + efficiency
                //                + "<br> Positive : " + positive
                //                + "<br> Negative : " + negative
                //                + "<br> Postive percentage : " + f.format(1.0 * positive / population) + " %"
                + "<br><br> " + errorReport
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
    private Object getRandomAndDifferent(Object[] list, Object differentThan, int endIndex, Random random) {
        Object obj = differentThan;

        if (list.length < 2 || endIndex < 2) {
            return null;
        }

        while (obj == null || obj.equals(differentThan)) {
            obj = list[random.nextInt(endIndex)];
        }

        return obj;
    }

    private void initializeMeshTopology(HierarchicalDirectedGraph graph, Node[][] students, int w, int h) {
        GraphModel graphModel = graph.getGraphModel();

        // create matrix of nodes (seats in room)
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w && (w * i + j < n); ++j) {
                // create node
                Node node = graph.getGraphModel().factory().newNode("" + (w * i + j));
                // initialize node
                node.getNodeData().setSize(20f);
                node.getNodeData().setLabel("" + (w * i + j));
                node.getNodeData().getAttributes().setValue("xy", i + ", " + j);

                students[i][j] = node;
                // add to graph
                graph.addNode(node);
            }
        }


        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w && (i * w + j < n); ++j) {
                Edge edge;

                // S
                if (neighborLinks[7]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i + 1][j]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                // SE
                if (neighborLinks[8]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i + 1][j - 1]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                // E
                if (neighborLinks[5]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i][j - 1]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                // NE
                if (neighborLinks[2]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i - 1][j - 1]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                // N
                if (neighborLinks[1]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i - 1][j]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                // NW
                if (neighborLinks[0]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i - 1][j + 1]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                // W
                if (neighborLinks[3]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i][j + 1]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                // SW
                if (neighborLinks[6]) {
                    try {
                        edge = graphModel.factory().newEdge(students[i][j], students[i + 1][j + 1]);
                        graph.addEdge(edge);
                    } catch (Exception ignore) { /**/ }
                }

                Progress.progress(progress, ++progressTick);
            }
        }
    }

    /**
     * Place the nodes on a grid of size w x h
     */
    private void runGridLayout(HierarchicalGraph graph, int w, int h) {

        graph.readLock();

        int areaSize = 1000;

        Node[] nodes = graph.getNodes().toArray();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w && (i * w + j) < n; j++) {
                nodes[w * i + j].getNodeData().setX(-areaSize / 2f + 1f * j / (w - 1) * areaSize);
                nodes[w * i + j].getNodeData().setY(-areaSize / 2f + 1f * i / (h - 1) * areaSize);//              
            }
        }
        nodes = null;

        graph.readUnlock();
    }

    /**
     * Map the original nodes to random locations in the mesh <br> Also copy
     * existing metrics over to the mesh <i>(modularity, centrality, degree
     * etc.)</i> <br>
     */
    private void randomizeNodePlacement(HierarchicalGraph originalGraph, HierarchicalGraph graph, Node[][] students, Random rand) {
        int i = 0, j = 0;

        // random-ordered list of nodes
        ArrayList<Node> originalNodes = new ArrayList<Node>();
        for (Node node : originalGraph.getNodes()) {
            originalNodes.add(rand.nextInt(originalNodes.size() + 1), node);
        }

        // map original nodes to mesh, also copy modularity       
        //Node[] nodes = graph.getNodes().toArray();        
        for (Node node : originalNodes) {
            // copy label
            students[i][j].getNodeData().setLabel(node.getNodeData().getLabel());
            int id = students[i][j].getId();
            graph.getNode(id).getNodeData().setLabel(node.getNodeData().getLabel());
            // copy other metrics, if present
            addMetric(node, graph.getNode(id), Modularity.MODULARITY_CLASS);
            addMetric(node, graph.getNode(id), EigenvectorCentrality.EIGENVECTOR);
            addMetric(node, graph.getNode(id), GraphDistance.BETWEENNESS);
            addMetric(node, graph.getNode(id), GraphDistance.CLOSENESS);
            addMetric(node, graph.getNode(id), GraphDistance.ECCENTRICITY);
            addMetric(node, graph.getNode(id), Degree.DEGREE);

//            addMetric(node, graph.getNode(i), Modularity.MODULARITY_CLASS);
//            addMetric(node, graph.getNode(i), EigenvectorCentrality.EIGENVECTOR);
//            addMetric(node, graph.getNode(i), GraphDistance.BETWEENNESS);
//            addMetric(node, graph.getNode(i), GraphDistance.CLOSENESS);
//            addMetric(node, graph.getNode(i), GraphDistance.ECCENTRICITY);
//            addMetric(node, graph.getNode(i), Degree.DEGREE);
//
//            i++;
            j++;
            if (j % w == 0) {
                i++;
                j = 0;
            }
        }
    }

    /**
     * Encapsulate a genetic solution
     */
    class GeneticSolution {

        private HierarchicalGraph originalGraph;
        //private HierarchicalDirectedGraph graph;
        private String[][] labels;
        private float score;
        private int w, h, n;

        public GeneticSolution(HierarchicalGraph originalGraph, Node[][] students, int w, int h, int n) {
            this.originalGraph = originalGraph;
            this.w = w;
            this.h = h;
            this.n = n;

            labels = new String[h][w];
            for (int i = 0; i < students.length; ++i) {
                for (int j = 0; j < students[i].length && (w * i + j < n); ++j) {
                    labels[i][j] = students[i][j].getNodeData().getLabel();
                }
            }
        }

        // test current setup
        private void computeEfficiency(HierarchicalDirectedGraph graph) {
            score = 0f;
            // get nodes with their underlying topology 
            Node[] nodes = graph.getNodes().toArray();
            // apply the local labels
            int i = 0, j = 0;
            for (Node node : nodes) {
                node.getNodeData().setLabel(labels[i][j]);
                j++;
                if (j % w == 0) {
                    i++;
                    j = 0;
                }
            }

            for (i = 0; i < h; ++i) {
                for (j = 0; j < w && (i * w + j < n); ++j) {

                    // out-edges of the current node: in the room mesh
                    EdgeIterable edgeIterR = ((HierarchicalDirectedGraph) graph).getOutEdges(nodes[w * i + j]);
                    // get corresponding original node
                    Node nodeO = getNodeByLabel(originalGraph, nodes[w * i + j].getNodeData().getLabel());

                    // compare neighbors
                    for (Edge eR : edgeIterR) {
                        // get node target in mesh
                        Node targetR = graph.getOpposite(nodes[w * i + j], eR);

                        // out-edges of the current node: in the original graph                                                           
                        EdgeIterable edgeIterO = ((HierarchicalGraph) originalGraph).getEdges(nodeO);

                        // iterate through all original targets                                         
                        for (Edge eO : edgeIterO) {
                            // get node target in original
                            Node targetO = originalGraph.getOpposite(nodeO, eO);

                            if (targetR.getNodeData().getLabel().equals(targetO.getNodeData().getLabel())) {
                                score += eO.getWeight();
                            }
                        }
                    }
                }
            }

            nodes = null;
        }

        /**
         * Apply the labels of this solution to the graph. <br> Also adds the
         * metrics from the original graph. <br>
         *
         * @param graph
         */
        public void applySolution(HierarchicalDirectedGraph graph, Node[][] students) {

            for (int i = 0; i < students.length; ++i) {
                for (int j = 0; j < students[i].length && (w * i + j < n); ++j) {

                    // copy label
                    int id = students[i][j].getId();
                    students[i][j].getNodeData().setLabel(labels[i][j]);
                    //students[i][j].getNodeData().setSize(20f);                    

                    //copy other metrics, if present
                    addMetric(getNodeByLabel(this.originalGraph, labels[i][j]), students[i][j], Modularity.MODULARITY_CLASS);
                    addMetric(getNodeByLabel(this.originalGraph, labels[i][j]), students[i][j], EigenvectorCentrality.EIGENVECTOR);
                    addMetric(getNodeByLabel(this.originalGraph, labels[i][j]), students[i][j], GraphDistance.BETWEENNESS);
                    addMetric(getNodeByLabel(this.originalGraph, labels[i][j]), students[i][j], GraphDistance.CLOSENESS);
                    addMetric(getNodeByLabel(this.originalGraph, labels[i][j]), students[i][j], GraphDistance.ECCENTRICITY);
                    addMetric(getNodeByLabel(this.originalGraph, labels[i][j]), students[i][j], Degree.DEGREE);
                }
            }
        }

        public float getScore() {
            return score;
        }

        /**
         * Clean the local graph
         */
        public void clean() {
            labels = null;
        }
    }

    class SolutionComparator implements Comparator<GeneticSolution> {

        private boolean ascending;

        public SolutionComparator(boolean ascending) {
            this.ascending = ascending;
        }

        public int compare(GeneticSolution gs1, GeneticSolution gs2) {

            Float score1 = gs1.getScore();
            Float score2 = gs2.getScore();

            // compare a 'statistic' of each target node
            if (ascending) {
                return score1.compareTo(score2);
            } else {
                return score2.compareTo(score1);
            }
        }
    }
}
