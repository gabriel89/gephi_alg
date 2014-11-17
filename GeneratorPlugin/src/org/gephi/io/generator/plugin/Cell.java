/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.generator.plugin;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.Progress;

/**
 *
 * @author Alexandru
 */
public class Cell implements Comparable<Object> {

    private GraphModel graphModel;
    private ArrayList<Node> nodes;
    private Random random;
    private int _id;
    private static int id = 0;

    public Cell(int size) {
        nodes = new ArrayList<Node>(size);
        random = new Random();
        _id = id;
        id++;
    }
    
    public Cell()
    {
        this(1);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Integer getSize() {
        return nodes.size();
    }

    public double getMeanDegree(Graph graph) {
        int avg = 0;
        for (Node node : nodes) {
            avg += graph.getDegree(node);
        }

        return 1.0 * avg / nodes.size();
    }

    /**
     * Create an integer with the following binary representation: <br> 8-bit
     * cell index | 24-bit node index <br> Maximum 256 cells, maximum 16M nodes
     * per cell
     *
     * @param cellId
     * @param nodeId
     * @return
     */
    public static long createIndex(int cellId, int nodeId) {
        long idx = cellId << 24 | nodeId;

        return idx;
    }

    public static int getCellIdFromIndex(long index) {
        int cellId = (int) (index >> 24);

        return cellId;
    }

    public static int getNodeIdFromIndex(long index) {
        int nodeId = (int) (index & 0xFFFFFF);

        return nodeId;
    }

    public static long crossoverIndices(Random random, long idx1, long idx2) {
        int cell1 = getCellIdFromIndex(idx1);
        int cell2 = getCellIdFromIndex(idx2);

        if (cell1 != cell2) {
            return -1;
        } else {
            int node1 = getNodeIdFromIndex(idx1);
            int node2 = getNodeIdFromIndex(idx2);

            int bits = (int) Math.ceil(Math.log(Math.max(node1, node2)) / Math.log(2)) + 1;
            int rleft = random.nextInt(bits);
            int rright = bits - rleft;

            int crossover = (node1 & (0xFFFFFF << rright)) | (node2 & (0xFFFFFF >> (24 - rright)));

            return createIndex(cell1, crossover);
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Cell) {
            return getSize().compareTo(((Cell) o).getSize());
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Cell) {
            return _id == ((Cell) obj)._id;
        } else {
            return false;
        }
    }
}
