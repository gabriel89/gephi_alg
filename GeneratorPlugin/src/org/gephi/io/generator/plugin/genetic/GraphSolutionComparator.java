/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.generator.plugin.genetic;

import java.util.Comparator;

/**
 *
 * @author Alexander
 */
public class GraphSolutionComparator implements Comparator<GraphSolution> {
   
    private boolean ascending;

    public GraphSolutionComparator(boolean ascending) {       
        this.ascending = ascending;
    }

    public int compare(GraphSolution solution1, GraphSolution solution2) {
       
        Double f1 = solution1.getFidelity();
        Double f2 = solution2.getFidelity();

        // compare a 'statistic' of each target node
        if (ascending) {
            return f1.compareTo(f2);
        } else {
            return f2.compareTo(f1);
        }
    }
}
