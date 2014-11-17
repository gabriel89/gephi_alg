package org.deltaalex.fractaldimension;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;

/**
 * Abstract encapsulation of fractals nodes and boxes.
 *
 * @author Alexandru Topirceanu
 */
public abstract class Fractal {

    protected Graph graph;
    protected boolean boxed;
    protected List<Fractal> neighbors;

    public Fractal(Graph graph) {
        this.graph = graph;
        boxed = false;
        neighbors = new ArrayList<Fractal>();
    }

    public void addNeighbor(Fractal neighbor) {
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }

    public boolean isBoxed() {
        return boxed;
    }

    public void setBoxed() {
        boxed = true;
    }

    public boolean isNeighbor(Fractal otherNode) {
        return neighbors.contains(otherNode);
    }

    protected abstract boolean addNode(Fractal newNode);

    protected ArrayList<Fractal> getVecinity(int depth) {
        ArrayList<Fractal> vecinity = new ArrayList<Fractal>();

        this.searchVecinity(vecinity, depth);
        vecinity.remove(this);

        return vecinity;
    }

    protected void searchVecinity(List<Fractal> vecinity, int depth) {
        if (depth > 0) {
            for (Fractal neighbor : neighbors) {
                if (!vecinity.contains(neighbor) && !vecinity.equals(this)) {
                    vecinity.add(neighbor);
                    neighbor.searchVecinity(vecinity, depth - 1);
                }
            }
        }
    }
}
