package org.deltaalex.fractaldimension.plugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import org.deltaalex.fractaldimension.FractalDimensioner;
import org.deltaalex.fractaldimension.Interpolation;
import org.gephi.data.attributes.api.*;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Alexandru Topirceanu
 */
public class FractalDimension implements Statistics, LongTask {

    private ProgressTicket progress;
    private boolean isCanceled;
    private double dimensionLinear, dimensionLog2;
    private int minBoxes = 1;
    private int maxBoxes = 16;
    private int graphDiameter;
    private boolean directed = false;
    private boolean checkCores = true;
    private int cores = 4;
    private Map<Integer, Integer> dimensions;

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public int getMinBoxes() {
        return minBoxes;
    }

    public void setMinBoxes(int minBoxes) {
        this.minBoxes = minBoxes;
    }

    public int getMaxBoxes() {
        return maxBoxes;
    }

    public void setMaxBoxes(int maxBoxes) {
        this.maxBoxes = maxBoxes;
    }

    public boolean getCheckCores() {
        return checkCores;
    }

    public void setCheckCores(boolean checkCores) {
        this.checkCores = checkCores;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalUndirectedGraph hgraph = graphModel.getHierarchicalUndirectedGraphVisible();
        execute(hgraph, attributeModel);
    }

    public void execute(HierarchicalUndirectedGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;
        Progress.start(progress);
        progress.switchToIndeterminate();
        hgraph.readLock();

        // average path length
        GraphDistance distance = new GraphDistance();
        distance.setNormalized(false);
        distance.setDirected(directed);
        distance.setProgressTicket(progress);
        distance.execute(hgraph.getGraphModel(), attributeModel);
        graphDiameter = (int) distance.getDiameter();

        // check box sizes
        if (minBoxes == -1) {
            minBoxes = 1;
        }
        if (maxBoxes == -1) {
            maxBoxes = graphDiameter;
        }

        final FractalDimensioner dimensioner = new FractalDimensioner();

        dimensions = dimensioner.runBoxCount(hgraph, minBoxes, maxBoxes, checkCores ? 0 : cores);

        // compute dimension (temporary)        
        Interpolation interpol = new Interpolation();

        double[] keys = new double[dimensions.keySet().size()];
        double[] values = new double[dimensions.keySet().size()];
        int i = 0;
        for (Integer key : dimensions.keySet()) {
            keys[i] = 1.0 * key;
            values[i] = 1.0 * dimensions.get(key);
            i++;
        }

        dimensionLinear = interpol.getSlopeForLinearScale(keys, values);
        
        dimensionLog2 = interpol.getSlopeForLogLogScale(keys, values, 2);

        hgraph.readUnlock();
        progress.switchToDeterminate(100);
        progress.finish();
    }

    public double getDimension() {
        return dimensionLog2;
    }

    public String getReport() {
        // chart on linear scale
        XYSeries dSeries = ChartUtils.createXYSeries(dimensions, "Fractal Dimension Distribution");

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "dB (fractal dimension distribution)",
                "lb (box dimension)",
                "Nb (number of boxes)",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        chart.setTitle("Linear scale");
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "fractal-dimension.png");

        // chart on loglog scale
        Map<Double, Double> dimensionsLogLog = new HashMap<Double, Double>();
        for (Integer key : dimensions.keySet()) {
            dimensionsLogLog.put(Math.log10(key), Math.log10(dimensions.get(key)));
        }

        dSeries = ChartUtils.createXYSeries(dimensionsLogLog, "Fractal Dimension Distribution");

        dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        chart = ChartFactory.createXYLineChart(
                "dB (fractal dimension distribution)",
                "lb (box dimension)",
                "Nb (number of boxes)",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        chart.setTitle("LogLog scale");
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile2 = ChartUtils.renderChart(chart, "fractal-dimension-loglog.png");


        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Fractal Dimension Report </h1> "
                + "<hr>"
                + "<h2> Parameters: </h2>"
                + "Min boxes: " + minBoxes + "<br>"
                + "Max boxes: " + maxBoxes + "<br>"
                + "Network diameter: " + graphDiameter + "<br>"
                + "Threads: " + (checkCores ? Runtime.getRuntime().availableProcessors() : cores) + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Fractal dimension (linear): " + f.format(dimensionLinear) + "<br>"
                + "Fractal dimension (loglog): " + f.format(dimensionLog2) + "<br>"
                + "<br /><br />" + imageFile
                + "<br /><br />" + imageFile2
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + " C. Song, S. Havlin and H. A. Makse, \"<i>Self-similarity of complex networks</i>\", Nature (London) 433, 392 (2005) <br />"
                + "<br /><br />" + "<h2> Implementation: </h2>"
                + "Alexandru Topirceanu, \"<i>Politehnica</i>\" University Timisoara <br />"
                + "</BODY> </HTML>";

        return report;
    }

}
