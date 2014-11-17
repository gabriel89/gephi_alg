package org.gephi.statistics.plugin.social;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.SEdge;
import org.gephi.graph.api.SNode;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

public class Socialize implements Statistics, LongTask {

    /**
     * Remembers if the Cancel function has been called.
     */
    private boolean isCanceled;
    /**
     * Keep track of the work done.
     */
    private ProgressTicket progress;
    /**
     *
     */
    private int population, positive, negative;
    // UI    
    private boolean keepColor = true;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public double getPopulation() {
        return population;
    }

    public boolean getKeepColor() {
        return keepColor;
    }

    public void setKeepColor(boolean keepColor) {
        this.keepColor = keepColor;
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Execution">
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        // test if current graph is a facebook graph
        graph.getAttributes().setValue(SNode.TAG_SOCIALIZED, true);

        isCanceled = false;       

        int i = 0;
        Random random = new Random();

        graph.readLock();
        Progress.start(progress, graph.getNodeCount());

        for (Node n : graph.getNodes()) {
            // temporary encapsulation
            SNode snode = new SNode(n);

            // initialize node model           
            float opinion = random.nextFloat();
            snode.setValue(SNode.Opinion, opinion);
            snode.setSleep();


            // resize stubborn agents
            Integer stubborn = snode.getValueAsInteger(SNode.Stubborn);
            if (stubborn != null && stubborn > 0) {
                snode.resetSize();
            }

            // set not stubborn
            snode.setValue(SNode.Stubborn, 0);

            if (opinion < 0.5f) {
                negative++;
                if (keepColor) {
                    snode.setColor(1 - opinion, opinion, 0);
                } else {
                    snode.setColor(1f, 0, 0);
                }

            } else {
                positive++;
                if (keepColor) {
                    snode.setColor(1 - opinion, opinion, 0);
                } else {
                    snode.setColor(0, 1f, 0);
                }
            }

            population++;

            // add trust towards each friend
            for (Edge edge : graph.getEdges(n)) {
                SEdge sedge = new SEdge(edge);
                // compute ranom float trust
                float trust = random.nextFloat();
                sedge.setValue(SEdge.Trust, trust);
            }

            if (isCanceled) {
                break;
            }
            i++;
            Progress.progress(progress, i);
        }

        graph.readUnlockAll();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Misc Area">
    private String errorReport = "";

    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.00");

        String report = "<HTML> <BODY> <h1>Opinion Report </h1> "
                + "<hr>"
                + "<br> <h2> Results: </h2>"
                + "Total Population : " + population
                + "<br> Positive : " + positive
                + "<br> Negative : " + negative
                + "<br> Postive percentage : " + f.format(1.0 * positive / population) + " %"
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
}
