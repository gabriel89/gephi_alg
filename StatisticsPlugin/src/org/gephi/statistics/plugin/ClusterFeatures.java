package org.gephi.statistics.plugin;

import static com.sun.org.apache.regexp.internal.RETest.test;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

public class ClusterFeatures implements Statistics, LongTask {

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
    // map that holds all node attributes for each modularity class
    // mod_class - attribute - value - count
    //    "0"    -   "HTA"   -  "0"  - 73 
    private HashMap<Object, HashMap<Object, HashMap<Object, Integer>>> attributes;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public double getPopulation() {
        return population;
    }

    // </editor-fold> 
    // <editor-fold defaultstate="collapsed" desc="Execution">
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        // test if current graph IS modularized
        Iterator<Node> iter = graph.getNodes().iterator();

        while (iter.hasNext()) {
            if (iter.next().getNodeData().getAttributes().getValue(Modularity.MODULARITY_CLASS) == null) {
                errorReport = "This graph is not modularized.\nPlease run <b>Modularity</b> first!";
                return;
            } else {
                break;
            }
        }

        isCanceled = false;
        attributes = new HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>();

        graph.readLock();
        Progress.start(progress, graph.getNodeCount());

        int i = 0;
        for (Node n : graph.getNodes()) {
            // get node attributes (cast to row because it is a richer interface)
            AttributeRow row = (AttributeRow) n.getAttributes();
            // get modularity id
            Object mod_class = row.getValue(Modularity.MODULARITY_CLASS);

            // if new modularity class
            if (attributes.get(mod_class) == null) {
                // create modularity entry in map
                attributes.put(mod_class, new HashMap<Object, HashMap<Object, Integer>>());
            }

            // update the attributes map with each attribute value
            for (AttributeValue value : row.getValues()) {
                String id = value.getColumn().getId();
                Object data = value.getValue();

                // if new attribute
                if (attributes.get(mod_class).get(id) == null) {
                    // create attribute entry in map
                    attributes.get(mod_class).put(id, new HashMap<Object, Integer>());
                }

                // if new value
                if (attributes.get(mod_class).get(id).get(data) == null) {
                    // create value entry with initial count = 1
                    attributes.get(mod_class).get(id).put(data, 1);
                } // else, update value counter
                else {
                    Integer count = attributes.get(mod_class).get(id).get(data);
                    count++;
                    attributes.get(mod_class).get(id).put(data, count);
                }
            }

            Progress.progress(progress, i++);
        }
        graph.readUnlockAll();

    }
// </editor-fold>   
    // <editor-fold defaultstate="collapsed" desc="Misc Area">
    private String errorReport = "";

    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.00");

        //StringBuilder sb = new StringBuilder();        
        String report = "<HTML> <BODY> <h1>Cluster Features Report </h1> "
                + "<hr><br>"
                + "Number of clusters : " + attributes.keySet().size();

        // create statistics for each modularity class        
        for (Object mod_class : attributes.keySet()) {
            report += "\n<h3>Modularity class " + mod_class + "</h3>\n";
            int population = attributes.get(mod_class).get("id").size();
            report += "<b>Population</b>: " + population + "\n";

            int index = 1;
            // for each attribute
            for (Object atr : attributes.get(mod_class).keySet()) {
                // omit common data: id, label, degree, modularity_class
                String satr = String.valueOf(atr);
                if (satr.toLowerCase().equals("id")
                        || satr.toLowerCase().equals("label")
                        || satr.toLowerCase().equals("degree")
                        || satr.toLowerCase().equals(Modularity.MODULARITY_CLASS)) {
                    continue;
                }

                // e.g. "1. HTA: "
                report += "<b>" + index + ". " + atr + "</b>: ";

                // order values in descending order
                SortedSet<Object> sortedValues = new TreeSet<Object>();
                // for each attribute value
                for (Object value : attributes.get(mod_class).get(atr).keySet()) {
                    if (value != null) {
                        sortedValues.add(value);
                    }
                }

                // local variable for detecting representative values                
                Object relevantValue = null;

                // iterate in ascending order
                for (Object sorted : sortedValues) {
                    // set current value as representative if more than 50% of population has it
                    int count = attributes.get(mod_class).get(atr).get(sorted);
                    String html = " <font color=\"black\">";

                    // color text accordingly to relevance (50%, 75%)
                    if (1.0 * count / population > 0.5001) {
                        relevantValue = sorted;
                        if (1.0 * count / population > 0.75) {
                            html += " <font color=\"red\">";
                        } else {
                            html += " <font color=\"orange\">";
                        }
                    }

                    // report value [count], for all values
                    report += html + sorted + " [" + attributes.get(mod_class).get(atr).get(sorted) + "]</font>; ";
                }
                report += "\n";
                index++;
            }
        }

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
}
