/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.deltaalex.fractaldimension;

import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Alexandru Topirceanu
 */
public class FractalNode extends Fractal {

    private Node node;
    private List<Fractal> fractalNodes;

    public FractalNode(Graph graph, List<Fractal> fractalNodes, Node node) {
        super(graph);
        this.fractalNodes = fractalNodes;
        this.node = node;
    }
    
    @Override
    protected void searchVecinity(List<Fractal> vecinity, int depth) {
        if (depth > 0) {
            for (Node neighbor : graph.getNeighbors(node)) {

                if (!node.equals(neighbor)) {
                    Fractal fractalNode = getParentofNode(neighbor);

                    if (!this.equals(fractalNode)) {
                        if (!vecinity.contains(fractalNode)) {
                            vecinity.add(fractalNode);
                            fractalNode.searchVecinity(vecinity, depth - 1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean addNode(Fractal newNode) {
        return false;
    }

    private Fractal getParentofNode(Node searchNode) {
        for (Fractal node : fractalNodes) {
            if (((FractalNode) node).isParentofNode(searchNode)) {
                return node;
            }
        }
        return null;
    }

    private boolean isParentofNode(Node node) {
        return this.node.equals(node);
    }

    public void initNeighbors() {
        Node[] vecinity = graph.getNeighbors(node).toArray();

        for (Fractal fractal : fractalNodes) {
            if (!this.equals(fractal)) {

                for (Node neighbor : vecinity) {

                    if (neighbor.equals(((FractalNode) fractal).node)) {
                        this.addNeighbor(fractal);
                        fractal.addNeighbor(this);
                    }
                }
            }
        }
    }
}
