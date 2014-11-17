package org.gephi.statistics.plugin.social;

import org.gephi.statistics.plugin.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Adds power-law distributed weights on a social network based on the
 * normalized betweenness of nodes in each dyad. <br> TODO: Determines influence
 * of nodes in social network based on community heterogeneity <br>
 *
 * @author Alexander
 */
public class SocialInfluenceLayer implements Statistics, LongTask {

    /**
     * Remembers if the Cancel function has been called.
     */
    private boolean isCanceled;
    /**
     * Keep track of the work done.
     */
    private ProgressTicket progress;
    /**
     * Edge type
     */
    private boolean directed = false;
    /*
     * List of enabled fitnesses
     */
    private List<Fitness> fitnesses;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">   
    public void setFitnesses(List<Fitness> fitnesses) {
        this.fitnesses = fitnesses;
    }

    public List<Fitness> getFitnesses() {
        return fitnesses;
    }

    private Double getDegree(Node node) {
        return (Double) node.getAttributes().getValue(Degree.DEGREE);
    }

    private Double getBetweenness(Node node) {
        return (Double) node.getAttributes().getValue(GraphDistance.BETWEENNESS);
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Execution">
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {

        graph.readLock();
        Progress.start(progress);
        progress.switchToIndeterminate();

        // degree        
        if (fitnesses.contains(Fitness.DEGREE)) {
            Degree degree = new Degree();
            degree.setProgressTicket(progress);
            degree.execute(graph.getGraphModel(), attributeModel);
            degree.getAverageDegree();
        }

        // betweenness, closeness
        if (fitnesses.contains(Fitness.BETWEENNESS) || fitnesses.contains(Fitness.CLOSENESS)) {
            GraphDistance distance = new GraphDistance();
            distance.setNormalized(false);
            distance.setDirected(directed);
            distance.setProgressTicket(progress);
            distance.execute(graph.getGraphModel(), attributeModel);
            distance.getPathLength();
        }

        // eigenvdctor
        if (fitnesses.contains(Fitness.EIGENVECTOR)) {
            EigenvectorCentrality eigen = new EigenvectorCentrality();
            eigen.setDirected(directed);
            eigen.setNumRuns(100);
            eigen.setProgressTicket(progress);
            eigen.execute(graph.getGraphModel(), attributeModel);
            eigen.getNumRuns();
        }

        // clustering coefficient
        if (fitnesses.contains(Fitness.CLUSTERING)) {
            ClusteringCoefficient clustering = new ClusteringCoefficient();
            clustering.setDirected(directed);
            clustering.setProgressTicket(progress);
            clustering.execute(graph.getGraphModel(), attributeModel);
            clustering.getAverageClusteringCoefficient();
        }

        // modularity                       
//        Modularity modularity = new Modularity();
//        modularity.setRandom(true);
//        modularity.setUseWeight(false);
//        modularity.setResolution(1.0);
//        modularity.setProgressTicket(progress);
//        modularity.execute(graph.getGraphModel(), attributeModel);
//        double mod = modularity.getModularity();

        // density
//        GraphDensity density = new GraphDensity();
//        density.setDirected(directed);
//        density.execute(graph.getGraphModel(), attributeModel);
//        double dns = density.getDensity();

        // diameter        
//        double dmt = distance.getDiameter();

        progress.switchToDeterminate(100);
        progress.finish();
        graph.readUnlockAll();

        //
        // compute weights
        //

        for (Edge edge : graph.getEdges()) {
            double weight;

            // based only on target's fitness
            if (directed) {
                // source node
                Node me = edge.getSource();
                double totalFitness = 0;

                // source's neighbors
                for (Node friend : graph.getNeighbors(me)) {
                    totalFitness += getBetweenness(friend);
                }

                weight = getBetweenness(edge.getTarget()) / (totalFitness + 0.0001);

            } // based on both source and target fitness
            else {
                ////// source node \\\\\\
                Node me = edge.getSource();
                double totalFitness = 0;

                // source's neighbors
                for (Node friend : graph.getNeighbors(me)) {
                    totalFitness += getBetweenness(friend);
                }

                weight = getBetweenness(edge.getTarget()) / (totalFitness + 0.0001);

                ////// target node \\\\\\
                me = edge.getTarget();
                totalFitness = 0f;

                // target's neighbors
                for (Node friend : graph.getNeighbors(me)) {
                    totalFitness += getBetweenness(friend);
                }

                weight += getBetweenness(edge.getSource()) / (totalFitness + 0.0001);
                weight /= 2f;
            }

            edge.setWeight((float) weight);
        }


    }
    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="Misc Area">
    private String errorReport = "";

    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.0000");

        String report = "<HTML> <BODY> <h1>Social Influence Report </h1> "
                + "<hr><br>";

        report += "<table border=\"1\"><tr><th></th>";
        report += "<th>ADeg</th>"
                + "<th>APL</th>"
                + "<th>CC</th>"
                + "<th>Mod</th>"
                + "<th>Dns</th>"
                + "<th>Dmt</th>"
                + "</tr>";

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

    public static enum Fitness {

        DEGREE, BETWEENNESS, EIGENVECTOR, CLOSENESS, CLUSTERING
    }
}
