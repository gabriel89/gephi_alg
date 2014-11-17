package org.gephi.statistics.plugin.social;

import org.gephi.statistics.plugin.social.diffusion.DiscreteComplexDiffusionImpl;
import org.gephi.statistics.plugin.social.diffusion.AbstractDiffusionImpl;
import org.gephi.statistics.plugin.social.diffusion.DiscreteSimpleDiffusionImpl;
import org.gephi.statistics.plugin.social.diffusion.ContinuousSimpleDiffusionImpl;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.SNode;
import org.gephi.statistics.plugin.social.diffusion.ContinuousComplexDiffusionImpl;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

public class Diffusion implements Statistics, LongTask {

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
    private AbstractDiffusionImpl diffusion;
    /**
     *
     */
    private int population, positive, negative;
    // UI
    private int diffuse = 0;
    private boolean animate = false;
    private int animationDelay = 15; 

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public double getPositiveRatio() {
        return population > 0 ? 1.0 * positive / population : Double.NaN;
    }

    public int getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(int diffuse) {
        this.diffuse = diffuse;
    }

    public boolean getAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public int getAnimationDelay() {
        return animationDelay;
    }

    public void setAnimationDelay(int animationDelay) {
        this.animationDelay = animationDelay;
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Execution">
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        // test if current graph is a facebook graph                
        if (graph.getAttributes().getValue(SNode.TAG_SOCIALIZED) == null) {
            errorReport = "ERROR! This statistic can only run on Social graphs! \n"
                    + "Convert one using the <b>Socialize Graph</b> statistic";
            return;
        } else {
            errorReport = "";
        }

        isCanceled = false;
        Progress.start(progress, graph.getNodeCount());
        graph.readUnlockAll();        

        // simple diffusion  
        if (diffuse == 0) {
            diffusion = new DiscreteSimpleDiffusionImpl(graph, animate, animationDelay);
        } else if (diffuse == 1) {
            diffusion = new DiscreteComplexDiffusionImpl(graph, animate, animationDelay);
        } else if (diffuse == 2) {
            diffusion = new ContinuousSimpleDiffusionImpl(graph, animate, animationDelay);
        } else if (diffuse == 3) {
            diffusion = new ContinuousComplexDiffusionImpl(graph, animate, animationDelay);
        } else {
            errorReport += "\n\n Wrong diffusion model ID \n\t Please contact support";
            return;
        }

        diffusion.start();
        try {
            diffusion.join();
        } catch (InterruptedException ex) {
            // no way
        }

        graph.readLock();
        for (Node n : graph.getNodes()) {
            // temporary encapsulation
            SNode snode = new SNode(n);

            if (snode.getValueAsFloat(SNode.Opinion) < 0.5f) {
                negative++;
            } else {
                positive++;
            }
            population++;
        }
        graph.readUnlock();

        Progress.finish(progress);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Misc Area">
    private String errorReport = "";

    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.00");

        String report = "<HTML> <BODY> <h1>Opinion Report </h1> "
                + "<hr>"
                + "<br> <h2> Results after diffusion: </h2>"
                + "Total Population : " + population
                + "<br> Positive : " + positive
                + "<br> Negative : " + negative
                + "<br> Postive percentage : " + f.format(1.0 * positive / population) + " %"
                //+ "<br><br><i> Using Node Sleep Interval : [" + SNode. + ", " + maxSleep + "]</i>"
                + "<br><br><font color=\"red\">" + errorReport + "</font>"
                + "</BODY></HTML>";

        return report;
    }

    /**
     *
     * @return
     */
    public boolean cancel() {
        this.isCanceled = true;
        if (diffusion != null) {
            diffusion.stopRunning();
        }
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
