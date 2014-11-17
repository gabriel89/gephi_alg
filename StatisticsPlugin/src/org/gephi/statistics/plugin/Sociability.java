package org.gephi.statistics.plugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class Sociability implements Statistics, LongTask {

    public static final String BETWEENNESS = "betweenesscentrality";
    public static final String CLOSENESS = "closnesscentrality";
    public static final String ECCENTRICITY = "eccentricity";
    public static final String AVERAGE_DEGREE = "avgdegree";
    /** */
    private double[] betweenness;
    /** */
    private double[] closeness;
    /** */
    private double[] eccentricity;
    /** */
    private int diameter;
    private int radius;
    private double avgDegree;
    private double avgClusteringCoeff;
    /** */
    private double avgDist;
    private double sociability;
    /** */
    private int N;
    /** */
    private boolean isDirected;
    /** */
    private ProgressTicket progress;
    /** */
    private boolean isCanceled;
    private int shortestPaths;
    private boolean isNormalized;

    public Sociability() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public double getSociability() {
        
        sociability = (avgDist*diameter*avgDegree);
        
        return sociability;
    }
    
    public double getAverageClusteringCoefficient() {
        return avgClusteringCoeff;
    }
    
    public double getPathLength() {
        return avgDist;
    }

    /**
     * 
     * @return
     */
    public double getDiameter() {
        return diameter;
    }
    
    /**
     *
     * @return
     */
    public double getAverageDegree() {
        return avgDegree;
    }

    /**
     *
     * @param graphModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = null;
        if (isDirected) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn eccentricityCol = nodeTable.getColumn(ECCENTRICITY);
        AttributeColumn closenessCol = nodeTable.getColumn(CLOSENESS);
        AttributeColumn betweenessCol = nodeTable.getColumn(BETWEENNESS);
        
        if(isDirected)
            bruteForce(hgraph, attributeModel);
        else
            triangles(hgraph);
        
        
        if (eccentricityCol == null) {
            eccentricityCol = nodeTable.addColumn(ECCENTRICITY, "Eccentricity", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }
        if (closenessCol == null) {
            closenessCol = nodeTable.addColumn(CLOSENESS, "Closeness Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }
        if (betweenessCol == null) {
            betweenessCol = nodeTable.addColumn(BETWEENNESS, "Betweenness Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        hgraph.readLock();

        N = hgraph.getNodeCount();

        betweenness = new double[N];
        eccentricity = new double[N];
        closeness = new double[N];
        diameter = 0;
        avgDist = 0;
        shortestPaths = 0;
        radius = Integer.MAX_VALUE;
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            index++;
        }

        Progress.start(progress, hgraph.getNodeCount());
        int count = 0;
        for (Node s : hgraph.getNodes()) {
            Stack<Node> S = new Stack<Node>();

            LinkedList<Node>[] P = new LinkedList[N];
            double[] theta = new double[N];
            int[] d = new int[N];
            for (int j = 0; j < N; j++) {
                P[j] = new LinkedList<Node>();
                theta[j] = 0;
                d[j] = -1;
            }

            int s_index = indicies.get(s);

            theta[s_index] = 1;
            d[s_index] = 0;

            LinkedList<Node> Q = new LinkedList<Node>();
            Q.addLast(s);
            while (!Q.isEmpty()) {
                Node v = Q.removeFirst();
                S.push(v);
                int v_index = indicies.get(v);

                EdgeIterable edgeIter = null;
                if (isDirected) {
                    edgeIter = ((HierarchicalDirectedGraph) hgraph).getOutEdgesAndMetaOutEdges(v);
                } else {
                    edgeIter = hgraph.getEdgesAndMetaEdges(v);
                }

                for (Edge edge : edgeIter) {
                    Node reachable = hgraph.getOpposite(v, edge);

                    int r_index = indicies.get(reachable);
                    if (d[r_index] < 0) {
                        Q.addLast(reachable);
                        d[r_index] = d[v_index] + 1;
                    }
                    if (d[r_index] == (d[v_index] + 1)) {
                        theta[r_index] = theta[r_index] + theta[v_index];
                        P[r_index].addLast(v);
                    }
                }
            }
            double reachable = 0;
            for (int i = 0; i < N; i++) {
                if (d[i] > 0) {
                    avgDist += d[i];
                    eccentricity[s_index] = (int) Math.max(eccentricity[s_index], d[i]);
                    closeness[s_index] += d[i];
                    diameter = Math.max(diameter, d[i]);
                    reachable++;
                }
            }

            radius = (int) Math.min(eccentricity[s_index], radius);

            if (reachable != 0) {
                closeness[s_index] /= reachable;
            }

            shortestPaths += reachable;

            double[] delta = new double[N];
            while (!S.empty()) {
                Node w = S.pop();
                int w_index = indicies.get(w);
                ListIterator<Node> iter1 = P[w_index].listIterator();
                while (iter1.hasNext()) {
                    Node u = iter1.next();
                    int u_index = indicies.get(u);
                    delta[u_index] += (theta[u_index] / theta[w_index]) * (1 + delta[w_index]);
                }
                if (w != s) {
                    betweenness[w_index] += delta[w_index];
                }
            }
            count++;
            if (isCanceled) {
                hgraph.readUnlockAll();
                return;
            }
            Progress.progress(progress, count);
        }

        avgDist /= shortestPaths;//mN * (mN - 1.0f);

        for (Node s : hgraph.getNodes()) {
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            int s_index = indicies.get(s);

            if (!isDirected) {
                betweenness[s_index] /= 2;
            }
            if (isNormalized) {
                closeness[s_index] = (closeness[s_index] == 0) ? 0 : 1.0 / closeness[s_index];
                betweenness[s_index] /= isDirected ? (N - 1) * (N - 2) : (N - 1) * (N - 2) / 2;
            }
            row.setValue(eccentricityCol, eccentricity[s_index]);
            row.setValue(closenessCol, closeness[s_index]);
            row.setValue(betweenessCol, betweenness[s_index]);
        }
        hgraph.readUnlock();
    }
    
    private int[] triangles;
    private ArrayWrapper[] network;
    private int K;
    private double[] nodeClustering;
    private int totalTriangles;
    
    /**
     *
     * @param v - The specific node to count the triangles on.
     */
    private void newVertex(int v) {
        int[] A = new int[N];

        for (int i = network[v].length() - 1; (i >= 0) && (network[v].get(i) > v); i--) {
            int neighbor = network[v].get(i);
            A[neighbor] = network[v].getCount(i);
        }
        for (int i = network[v].length() - 1; i >= 0; i--) {
            int neighbor = network[v].get(i);
            for (int j = closest_in_array(neighbor); j >= 0; j--) {
                int next = network[neighbor].get(j);
                if (A[next] > 0) {
                    triangles[next] += network[v].getCount(i);
                    triangles[v] += network[v].getCount(i);
                    triangles[neighbor] += A[next];
                }
            }
        }
    }
    
    private int closest_in_array(int v) {
        int right = network[v].length() - 1;

        /* optimization for extreme cases */
        if (right < 0) {
            return (-1);
        }
        if (network[v].get(0) >= v) {
            return (-1);
        }
        if (network[v].get(right) < v) {
            return (right);
        }
        if (network[v].get(right) == v) {
            return (right - 1);
        }

        int left = 0, mid;
        while (right > left) {
            mid = (left + right) / 2;
            if (v < network[v].get(mid)) {
                right = mid - 1;
            } else if (v > network[v].get(mid)) {
                left = mid + 1;
            } else {
                return (mid - 1);
            }
        }


        if (v > network[v].get(right)) {
            return (right);
        } else {

            return right - 1;
        }
    }
    
    private void tr_link_nohigh(int u, int v, int count) {
        int iu = 0, iv = 0, w;
        while ((iu < network[u].length()) && (iv < network[v].length())) {
            if (network[u].get(iu) < network[v].get(iv)) {
                iu++;
            } else if (network[u].get(iu) > network[v].get(iv)) {
                iv++;
            } else { /* neighbor in common */
                w = network[u].get(iu);
                if (w >= K) {
                    triangles[w] += count;
                }
                iu++;
                iv++;
            }
        }
    }
    
    public void triangles(HierarchicalGraph hgraph) {

        int ProgressCount = 0;
        Progress.start(progress, 7 * hgraph.getNodeCount());

        hgraph.readLock();

        N = hgraph.getNodeCount();
        nodeClustering = new double[N];

        /** Create network for processing */
        network = new ArrayWrapper[N];

        /**  */
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            network[index] = new ArrayWrapper();
            index++;
            Progress.progress(progress, ++ProgressCount);
        }

        index = 0;
        for (Node node : hgraph.getNodes()) {
            HashMap<Node, EdgeWrapper> neighborTable = new HashMap<Node, EdgeWrapper>();

            if (!isDirected) {
                for (Edge edge : hgraph.getEdgesAndMetaEdges(node)) {
                    Node neighbor = hgraph.getOpposite(node, edge);
                    neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                }
            } else {
                for (Edge in : ((HierarchicalDirectedGraph) hgraph).getInEdgesAndMetaInEdges(node)) {
                    Node neighbor = in.getSource().getNodeData().getNode(hgraph.getView().getViewId());
                    neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                }

                for (Edge out : ((HierarchicalDirectedGraph) hgraph).getOutEdgesAndMetaOutEdges(node)) {
                    Node neighbor = out.getTarget().getNodeData().getNode(hgraph.getView().getViewId());
                    EdgeWrapper ew = neighborTable.get(neighbor);
                    if (ew == null) {
                        neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                    } else {
                        ew.count++;
                    }
                }
            }

            EdgeWrapper[] edges = new EdgeWrapper[neighborTable.size()];
            int i = 0;
            for (EdgeWrapper e : neighborTable.values()) {
                edges[i] = e;
                i++;
            }
            network[index].node = node;
            network[index].setArray(edges);
            index++;
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                hgraph.readUnlockAll();
                return;
            }
        }

        Arrays.sort(network);
        for (int j = 0; j < N; j++) {
            network[j].setID(j);
            Progress.progress(progress, ++ProgressCount);
        }

        for (int j = 0; j < N; j++) {
            Arrays.sort(network[j].getArray(), new Renumbering());
            Progress.progress(progress, ++ProgressCount);
        }

        triangles = new int[N];
        K = (int) Math.sqrt(N);


        for (int v = 0; v < K && v < N; v++) {
            newVertex(v);
            Progress.progress(progress, ++ProgressCount);
        }

        /* remaining links */
        for (int v = N - 1; (v >= 0) && (v >= K); v--) {
            for (int i = closest_in_array(v); i >= 0; i--) {
                int u = network[v].get(i);
                if (u >= K) {
                    tr_link_nohigh(u, v, network[v].getCount(i));
                }
            }
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                hgraph.readUnlockAll();
                return;
            }
        }

        //Results and average
        avgClusteringCoeff = 0;
        totalTriangles = 0;
        int numNodesDegreeGreaterThanOne = 0;
        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                numNodesDegreeGreaterThanOne++;
                double cc = triangles[v];
                totalTriangles += triangles[v];
                cc /= (network[v].length() * (network[v].length() - 1));
                if (!isDirected) {
                    cc *= 2.0f;
                }
                nodeClustering[v] = cc;
                avgClusteringCoeff += cc;
            }
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                hgraph.readUnlockAll();
                return;
            }
        }
        totalTriangles /= 3;
        avgClusteringCoeff /= numNodesDegreeGreaterThanOne;

        hgraph.readUnlock();
    }
    
    private void bruteForce(HierarchicalGraph hgraph, AttributeModel attributeModel) {
    //The atrributes computed by the statistics
    AttributeTable nodeTable = attributeModel.getNodeTable();
    AttributeColumn clusteringCol = nodeTable.getColumn("clustering");
    if (clusteringCol == null) {
    clusteringCol = nodeTable.addColumn("clustering", "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
    }
    
    float totalCC = 0;
    
    hgraph.readLock();
    
    Progress.start(progress, hgraph.getNodeCount());
    int node_count = 0;
    for (Node node : hgraph.getNodes()) {
    float nodeCC = 0;
    int neighborhood = 0;
    NodeIterable neighbors1 = hgraph.getNeighbors(node);
    for (Node neighbor1 : neighbors1) {
    neighborhood++;
    NodeIterable neighbors2 = hgraph.getNeighbors(node);
    for (Node neighbor2 : neighbors2) {
    
    if (neighbor1 == neighbor2) {
    continue;
    }
    if (isDirected) {
    if (((HierarchicalDirectedGraph) hgraph).getEdge(neighbor1, neighbor2) != null) {
    nodeCC++;
    }
    if (((HierarchicalDirectedGraph) hgraph).getEdge(neighbor2, neighbor1) != null) {
    nodeCC++;
    }
    } else {
    if (hgraph.isAdjacent(neighbor1, neighbor2)) {
    nodeCC++;
    }
    }
    }
    }
    nodeCC /= 2.0;
    
    if (neighborhood > 1) {
    float cc = nodeCC / (.5f * neighborhood * (neighborhood - 1));
    if (isDirected) {
    cc = nodeCC / (neighborhood * (neighborhood - 1));
    }
    
    AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
    row.setValue(clusteringCol, cc);
    
    totalCC += cc;
    }
    
    if (isCanceled) {
    break;
    }
    
    node_count++;
    Progress.progress(progress, node_count);
    
    }
    avgClusteringCoeff = totalCC / hgraph.getNodeCount();
    
    hgraph.readUnlockAll();
    }

    public void setNormalized(boolean isNormalized) {
        this.isNormalized = isNormalized;
    }

    public boolean isNormalized() {
        return isNormalized;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    private String createImageFile(TempDir tempDir, double[] pVals, String pName, String pX, String pY) {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<Double, Integer>();
        for (int i = 0; i < N; i++) {
            Double d = pVals[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, pName);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                pName,
                pX,
                pY,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, isNormalized);
        return ChartUtils.renderChart(chart, pName + ".png");
    }

    /**
     *
     * @return
     */
    public String getReport() {
        String htmlIMG1 = "";
        String htmlIMG2 = "";
        String htmlIMG3 = "";
        try {
            TempDir tempDir = TempDirUtils.createTempDir();
            htmlIMG1 = createImageFile(tempDir, betweenness, "Betweenness Centrality Distribution", "Value", "Count");
            htmlIMG2 = createImageFile(tempDir, closeness, "Closeness Centrality Distribution", "Value", "Count");
            htmlIMG3 = createImageFile(tempDir, eccentricity, "Eccentricity Distribution", "Value", "Count");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        String report = "<HTML> <BODY> <h1>Graph Distance  Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br />"
                + "<br /> <h2> Results: </h2>"
                + "Diameter: " + diameter + "<br />"
                + "Radius: " + radius + "<br />"
                + "Average Path length: " + avgDist + "<br />"
                + "Number of shortest paths: " + shortestPaths + "<br /><br />"
                + htmlIMG1 + "<br /><br />"
                + htmlIMG2 + "<br /><br />"
                + htmlIMG3
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + "Ulrik Brandes, <i>A Faster Algorithm for Betweenness Centrality</i>, in Journal of Mathematical Sociology 25(2):163-177, (2001)<br />"
                + "</BODY> </HTML>";

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

    /**
     *
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
