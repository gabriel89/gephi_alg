package org.deltaalex.fractaldimension;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;

/**
 * A box is a group of nodes in which no single path between any two nodes is
 * longer than a given distance
 *
 * @author Alexandru Topirceanu
 */
public class FractalBox extends Fractal {

    private int distance;
    private List<Fractal> nodes;   

    public FractalBox(Graph graph, int distance) {
        super(graph);
        this.distance = distance;        
        nodes = new ArrayList<Fractal>();
    }

    /**
     * Verify distance between this node and all other nodes in the box
     *
     * @param node
     * @return
     */
    public boolean addNode(Fractal newNode) {
        // misc
        if (nodes.contains(newNode)) {
            return true;
        }

        // get vecinity of new node
        ArrayList<Fractal> vecinity = newNode.getVecinity(distance);

        // its vecinit must contain all nodes in the box
        for (Fractal node : nodes) {
            if (!vecinity.contains(node)) {
                return false;
            }
        }

        // add to box
        nodes.add(newNode);
        newNode.setBoxed();
        return true;
    }
      
    public List<Fractal> getNodes() {
        return nodes;
    }    
}
