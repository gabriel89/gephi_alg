/**
 * Creates a WSDD network
 */
package org.gephi.io.generator.plugin;

import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Watts-Strogatz small-world network with degree distribution <br>
 * http://www.physics.fudan.edu.cn/tps/people/jphuang/Mypapers/JPA-1.pdf
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class WSDDGraph extends AbstractGraph implements Generator {

    private int numberOfCells = 20;
    private int avgCellSize = 10;
    private int K = 2;
    private double wiringProbability = 0.1;

    @Override
    protected int initialize() {
        return 2 * numberOfCells;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        // cell strucutre
        Cell[] cells = new Cell[numberOfCells];
        int size = 0;

        // initialize each cell
        for (int cell = 0; cell < numberOfCells; ++cell) {
            int n = getPowerDistributedIntegerValue(random, avgCellSize / 3, 3 * avgCellSize);
            cells[cell] = new Cell(n);

            // add nodes to cell
            for (int i = 0; i < n; i++) {
                // create node
                Node node = graphModel.factory().newNode();
                // initialize node
                node.getNodeData().setSize(NODE_SIZE);
                node.getNodeData().setLabel("" + (size + i));
                // add to graph
                graphModel.getGraph().addNode(node);
                cells[cell].addNode(node);

                //Sleep some time
                animateNode();
            }

            // add intra-cell links to create a small-world
            if (wiringProbability > 0) {
                createSmallWorldCommunity(cells[cell], graphModel, random,
                        2, cells[cell].getSize() - 2, wiringProbability, false, false);
            }

            size += n;
            progressTick();
        }

        // create ring of cells
        for (int cell = 0; cell < numberOfCells; ++cell) {
            // link to K cells on the right
            for (int i = 1; i <= K; ++i) {
                // get a random node from the current cell
                Node source = cells[cell].getNodes().get(random.nextInt(cells[cell].getSize()));
                // get a random node from thhe neighboring cell
                int nextCell = (cell + i) % numberOfCells;
                Node target = cells[nextCell].getNodes().get(random.nextInt(cells[nextCell].getSize()));

                // add link 
                Edge newEdge = graphModel.factory().newEdge(source, target);
                graphModel.getGraph().addEdge(newEdge);
            }

            progressTick();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">

    public String getName() {
        return NbBundle.getMessage(WSDDGraph.class, "WSDDGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(WSDDGraphUI.class);
    }

    public void setNumberOfCells(int numberOfCells) {
        if (numberOfCells < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfCells = numberOfCells;
    }

    public void setAvgCellSize(int cellSize) {
        if (cellSize < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.avgCellSize = cellSize;
    }

    public void setKNeighbors(int K) {
        if (K < 0) {
            throw new IllegalArgumentException("# of neighbors must be greater than 0");
        }
        this.K = K;
    }

    public void setWiringProbability(double wiringProbability) {
        if (wiringProbability < 0 || wiringProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.wiringProbability = wiringProbability;
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public int getKNeighbors() {
        return K;
    }

    public int getAvgCellSize() {
        return avgCellSize;
    }

    public double getWiringProbability() {
        return wiringProbability;
    }

    // </editor-fold>
}
