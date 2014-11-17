/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.io.generator.plugin.genetic;

import java.util.ArrayList;
import org.gephi.graph.api.Edge;

/**
 *
 * @author Alexandru Topirceanu
 */
public class GraphSolution {

    private ArrayList<Edge> edges;
    private Double fidelity;

    public GraphSolution() {
        this.edges = new ArrayList<Edge>();
        this.fidelity = 0.0;
    }
    
    public void add(Edge edge)
    {
        edges.add(edge);
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setFidelity(Double fidelity) {
        this.fidelity = fidelity;
    }
            
    public Double getFidelity() {
        return fidelity;
    }
           
}
