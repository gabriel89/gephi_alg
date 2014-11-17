package org.deltaalex.fractaldimension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Alexandru Topirceanu
 */
public class FractalDimensioner {

    private List<Fractal> fractalNodes;

    public FractalDimensioner() {
        fractalNodes = new ArrayList<Fractal>();
    }

    /**
     * Iterates the the box count algorithm from the min value to the max value
     * of boxes. <br> The method runs on a thread pool. <br>
     *
     * @param minBoxCount - starting box size
     * @param maxBoxCount - ending box size (included)
     * @param threads Number of threads used to run the algorithm on. A value of
     * 0 indicates the algorithm will use the number of CPU cores.
     */
    public Map<Integer, Integer> runBoxCount(final Graph graph, int minBoxCount, int maxBoxCount, int threads) {
        ThreadPool pool;
        if (threads < 1) {
            pool = new ThreadPool(Runtime.getRuntime().availableProcessors());
        } else {
            pool = new ThreadPool(threads);
        }

        fractalNodes.clear();
        // initialize fractal nodes
        for (Node node : graph.getNodes()) {
            fractalNodes.add(new FractalNode(graph, fractalNodes, node));
        }
        // connect fractal nodes
        for (Fractal node : fractalNodes) {
            ((FractalNode) node).initNeighbors();
        }

        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int depth = minBoxCount; depth <= maxBoxCount; ++depth) {
            final int _depth = depth;
            pool.runTask(new Runnable() {
                @Override
                public void run() {
                    int n = getBoxCount(graph, fractalNodes, _depth, -1);
                    map.put(_depth, n);
                }
            });
        }

        pool.join();

        return map;
    }

    public int getBoxCount(Graph graph, List<Fractal> nodes, int depth, Integer lastBoxSize) {

        if (nodes.size() > 1) {
            List<Fractal> boxes = new ArrayList<Fractal>();
            boxes.add(new FractalBox(graph, depth));

            for (Fractal node : nodes) {
                if (!node.isBoxed()) {
                    // try to find a box for the node
                    for (Fractal box : boxes) {
                        if (box.addNode(node)) {
                            break;
                        }
                    }

                    // else create a new box
                    if (!node.isBoxed()) {
                        FractalBox box = new FractalBox(graph, depth);
                        box.addNode(node);
                        boxes.add(box);
                    }
                }
            }

            createBoxLinks(boxes);

            if (lastBoxSize > 0 && lastBoxSize.equals(boxes.size())) {
                return 0;

            } else {
                return boxes.size() + getBoxCount(graph, boxes, depth, boxes.size());
            }

        } else {
            return nodes.size();
        }
    }

    private void createBoxLinks(List<Fractal> boxes) {
        // for each box
        for (Fractal box : boxes) {
            // and all other boxes
            for (Fractal otherBox : boxes) {
                // if not same box
                if (!box.equals(otherBox)) {
                    // for every node in source box
                    for (Fractal node : ((FractalBox) box).getNodes()) {
                        // for every node in target box
                        for (Fractal otherNode : ((FractalBox) otherBox).getNodes()) {

                            if (node.isNeighbor(otherNode)) {
                                box.addNeighbor(otherBox);
                                otherBox.addNeighbor(box);
                            }
                        }
                    }
                }
            }
        }
    }
}
