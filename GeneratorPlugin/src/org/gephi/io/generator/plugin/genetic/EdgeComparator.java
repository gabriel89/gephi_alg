/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.generator.plugin.genetic;

import java.util.Comparator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.SNode;

/**
 *
 * @author Alexander
 */
public class EdgeComparator implements Comparator<Edge> {

    private String toCompare;
    private boolean ascending;

    public EdgeComparator(String toCompare, boolean ascending) {
        this.toCompare = toCompare;
        this.ascending = ascending;
    }

    public int compare(Edge edge1, Edge edge2) {

        // get edge target
        SNode node1 = new SNode(edge1.getTarget());
        SNode node2 = new SNode(edge2.getTarget());

        Double f1 = node1.getValueAsDouble(toCompare);
        Double f2 = node2.getValueAsDouble(toCompare);

        // compare a 'statistic' of each target node
        if (ascending) {
            return f1.compareTo(f2);
        } else {
            return f2.compareTo(f1);
        }
    }
}
