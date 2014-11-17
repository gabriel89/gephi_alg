/**
 * Creates a cellular network
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
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class CellularGraph extends AbstractGraph implements Generator {

    private int numberOfCells = 4;//12;
    private int avgCellSize = 5;//8;
    private double intraWiringProbability = 0.6;
    private double interWiringProbability = 0.4;

    @Override
    protected int initialize() {
        return 2 * numberOfCells * avgCellSize + numberOfCells;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        // leaders
        Node[] cellLeaders = new Node[numberOfCells];
        Cell[] cells = new Cell[numberOfCells];
        int size = 0;

        // initialize each cell
        for (int cell = 0; cell < numberOfCells; ++cell) {
            // create uniform random sized cell
            int n = random.nextInt(avgCellSize + 5) + 3;
            cells[cell] = new Cell(n);

            // add nodes to cell
            for (int i = 0; i < n; i++) {
                // create node
                Node node = graphModel.factory().newNode("" + (size + i));
                // initialize node
                node.getNodeData().setSize(NODE_SIZE);
                node.getNodeData().setLabel("" + (size + i));
                //node.getNodeData().setLabel("N" + (size + i));
                // add to graph
                graphModel.getGraph().addNode(node);
                cells[cell].addNode(node);

                //Sleep some time
                animateNode();
                progressTick();
            }

            // choose random leader for each cell
            int leader = random.nextInt(n);
            cellLeaders[cell] = cells[cell].getNodes().get(leader);

            // add intra-cell links
            if (intraWiringProbability > 0) {
                for (int i = 0; i < n - 1 && !cancel; i++) {
                    Node source = cells[cell].getNodes().get(i);
                    for (int j = i + 1; j < n && !cancel; j++) {
                        Node target = cells[cell].getNodes().get(j);

                        if (random.nextDouble() < intraWiringProbability) {
                            Edge newEdge = graphModel.factory().newEdge(source, target);
                            graphModel.getGraph().addEdge(newEdge);

                            animateEdge();
                        }
                    }
                    progressTick();
                }
            }
            size += n;
        }

        // connect cell leaders
        for (Node leader1 : cellLeaders) {
            for (Node leader2 : cellLeaders) {
                if (!leader1.equals(leader2)) {

                    // link leaders
                    if (random.nextDouble() > interWiringProbability) {
                        // create link 
                        Edge newEdge = graphModel.factory().newEdge(leader1, leader2);
                        graphModel.getGraph().addEdge(newEdge);

                        animateEdge();
                    }
                    progressTick();
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getName() {
        return NbBundle.getMessage(CellularGraph.class, "CellularGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(CellularGraphUI.class);
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

    public void setIntraWiringProbability(double intraWiringProbability) {
        if (intraWiringProbability < 0 || intraWiringProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.intraWiringProbability = intraWiringProbability;
    }

    public void setInterWiringProbability(double interWiringProbability) {
        if (interWiringProbability < 0 || interWiringProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.interWiringProbability = interWiringProbability;
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public int getAvgCellSize() {
        return avgCellSize;
    }

    public double getIntraWiringProbability() {
        return intraWiringProbability;
    }

    public double getInterWiringProbability() {
        return interWiringProbability;
    }
    // </editor-fold>
}
