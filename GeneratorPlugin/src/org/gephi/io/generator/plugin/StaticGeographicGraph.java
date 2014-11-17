/**
 * Creates a Static-Geographic network
 */
package org.gephi.io.generator.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.graph.api.*;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class StaticGeographicGraph extends AbstractGraph implements Generator {

    private final int Position = 1000;
    private final String SocialProb = "SocialProb";
    private final String ParanoidProb = "ParanoidProb";
    private int numberOfNodes = 200;
    private double maxLinkDistance = 0.3; // % of Position
    private double linkRequestProbability = 0.15;
    private double linkAcceptProbability = 0.65;

    @Override
    protected int initialize() {
        return numberOfNodes + numberOfNodes * numberOfNodes;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {

        // list of nodes
        ArrayList<Node> nodes = new ArrayList<Node>(numberOfNodes);

        // position inside circle
        for (int i = 0; i < numberOfNodes; ++i) {
            // generate random x,y
            int x = Position;
            int y = Position;

            while (x * x + y * y > Position * Position) {
                x = random.nextInt(Position) - Position / 2;
                y = random.nextInt(Position) - Position / 2;
            }

            // create node
            Node node = graphModel.factory().newNode();
            // initialize node
            node.getNodeData().setSize(NODE_SIZE);
            node.getNodeData().setLabel("" + (i));
            // get specific distribution probabilities
            node.getAttributes().setValue(SocialProb, getNormalDistributedNumber(random, linkRequestProbability));
            node.getAttributes().setValue(ParanoidProb, getNormalDistributedNumber(random, linkAcceptProbability));
            // add to graph
            graphModel.getGraph().addNode(node);
            nodes.add(node);
            node.getNodeData().setX(x);
            node.getNodeData().setY(y);

            //Sleep some time
            animateNode();
            progressTick();
        }

        // link nodes
        SNode snode1, snode2;
        for (Node source : nodes) {
            for (Node target : nodes) {
                if (!source.equals(target)) {
                    snode1 = new SNode(source);
                    snode2 = new SNode(target);

                    // get distance between nodes
                    double x1 = source.getNodeData().x();
                    double y1 = source.getNodeData().y();
                    double x2 = target.getNodeData().x();
                    double y2 = target.getNodeData().y();
                    double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

                    // if inside max distance
                    if (dist <= maxLinkDistance * Position) {

                        // check both probabilities and link
                        float ps1 = snode1.getValueAsFloat(SocialProb);
                        float pp2 = snode2.getValueAsFloat(ParanoidProb);

                        float ps = random.nextFloat();
                        if (ps /**
                                 * Position / dist
                                 */
                                <= ps1) {
                            float pp = random.nextFloat();
                            if (pp < pp2) {
                                Edge edge = graphModel.factory().newEdge(source, target);
                                graphModel.getGraph().addEdge(edge);

                                //Sleep some time
                                animateEdge();
                            }
                        }
                    }
                }
                progressTick();
            }
        }
        
//        // dbg       
//        try {
//            File fedges = new File("C:\\Users\\Alexander\\Desktop\\geo_" + numberOfNodes + ".in");
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
        
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getName() {
        return NbBundle.getMessage(StaticGeographicGraph.class, "StaticGeographicGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(StaticGeographicGraphUI.class);
    }

    public void setNumberOfNodes(int numberOfNodes) {
        if (numberOfNodes < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfNodes = numberOfNodes;
    }

    public void setMaxLinkDistance(double maxLinkDistance) {
        if (maxLinkDistance < 0 || maxLinkDistance > 1) {
            throw new IllegalArgumentException("Link probability must be between 0 and 1");
        }
        this.maxLinkDistance = maxLinkDistance;
    }

    public void setLinkRequestProbability(double linkRequestProbability) {
        if (linkRequestProbability < 0 || linkRequestProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.linkRequestProbability = linkRequestProbability;
    }

    public void setLinkAcceptProbability(double linkAcceptProbability) {
        if (linkAcceptProbability < 0 || linkAcceptProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.linkAcceptProbability = linkAcceptProbability;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public double getMaxLinkDistance() {
        return maxLinkDistance;
    }

    public double getLinkRequestProbability() {
        return linkRequestProbability;
    }

    public double getLinkAcceptProbability() {
        return linkAcceptProbability;
    }
    // </editor-fold>
}
