package org.gephi.io.generator.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Small-world network as defined by Watts-Strogatz <br>
 * http://en.wikipedia.org/wiki/Watts_and_Strogatz_model#Algorithm
 *
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class SmallWorldGraph extends AbstractGraph implements Generator {

    private int numberOfNodes = 300;
    private int K = 5;
    private double wiringProbability = 0.2;

    @Override
    protected int initialize() {
        return numberOfNodes + 1;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        // nodes
        Cell cell = new Cell(numberOfNodes);

        // create nodes
        for (int i = 0; i < numberOfNodes; ++i) {

            // create node
            Node node = graphModel.factory().newNode();
            // initialize node
            node.getNodeData().setSize(NODE_SIZE);
            node.getNodeData().setLabel("" + (i));
            // add to graph
            graphModel.getGraph().addNode(node);
            cell.addNode(node);

            //Sleep some time
            animateNode();
            progressTick();
        }

        createSmallWorldCommunity(cell, graphModel, random, K, K, wiringProbability, false, false);

//        // dbg       
//        try {
//            File fedges = new File("C:\\Users\\Alexander\\Desktop\\sw_" + numberOfNodes + "_K_" + K + ".in");
//            PrintWriter pw = new PrintWriter(fedges);
//
//            for (Edge edge : graphModel.getGraph().getEdges()) {
//                pw.println(edge.getSource().getId() + " " + edge.getTarget().getId());
//            }
//
//            pw.close();
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        // dbg

        progressTick();
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getName() {
        return NbBundle.getMessage(SmallWorldGraph.class, "SmallWorldGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(SmallWorldGraphUI.class);
    }

    public void setNumberOfNodes(int numberOfNodes) {
        if (numberOfNodes < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfNodes = numberOfNodes;
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

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getKNeighbors() {
        return K;
    }

    public double getWiringProbability() {
        return wiringProbability;
    }
    // </editor-fold>
}
