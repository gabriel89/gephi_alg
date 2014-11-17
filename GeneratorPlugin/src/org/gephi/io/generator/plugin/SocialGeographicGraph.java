/**
 * Creates a Social-Geographic network
 */
package org.gephi.io.generator.plugin;

import java.util.ArrayList;
import java.util.Random;
import org.gephi.graph.api.*;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The social-geographic graph creates a realistic social network based on the
 * following algorithm: <br>
 *
 * - create a static-geographic network (R=1000, N, p, density d, r = R*sqrt(d))
 * <br> - choose Nl leaders and connect each of them with Nd random delegates
 * outside the radius r <br> - apply A-Barabasi algorithm on initial graph made
 * out of leaders and delegates, by adding remaining nodes <br> - combine
 * static-geographic network with scale-free network, eliminate doubled edges *
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class SocialGeographicGraph extends AbstractGraph implements Generator {

    private final int R = 1000;
    private int numberOfNodes = 500; 
    private double linkProbability = 0.35;
    private double density = 0.05;
    private int numberOfLeaders = 5;
    private int numberOfDelegates = 50;

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
            // generate random x, y
            int x = R;
            int y = R;

            while (x * x + y * y > R * R) {
                x = random.nextInt(R) - R / 2;
                y = random.nextInt(R) - R / 2;
            }

            // create node
            Node node = graphModel.factory().newNode();
            // initialize node
            node.getNodeData().setSize(NODE_SIZE);
            node.getNodeData().setLabel("" + (i));
            // add to graph
            graphModel.getGraph().addNode(node);
            nodes.add(node);
            node.getNodeData().setX(x);
            node.getNodeData().setY(y);

            //Sleep some time
            animateNode();
            progressTick();
        }

        // link nodes: static-geographic
        double radius = 1.0 * R * Math.sqrt(density);

        for (Node source : nodes) {
            for (Node target : nodes) {
                if (!source.equals(target)) {

                    double dist = getDistance(source, target);

                    // if inside max distance
                    if (dist <= radius) {
                        // if probability
                        if (random.nextFloat() <= linkProbability) {
                            createUndirectedEdge(graphModel, source, target);
                        }
                    }
                }
                progressTick();
            }
        }

        // choose leaders and delegates        
        ArrayList<Node> sfNodes = new ArrayList<Node>();
        ArrayList<Node> leaders = new ArrayList<Node>();

        // leaders
        for (int i = 0; i < numberOfLeaders; ++i) {
            // choose random unique leader
            Node leader = nodes.get(random.nextInt(nodes.size()));
            if (!leaders.contains(leader)) {
                leaders.add(leader);
                nodes.remove(leader);
            }
        }

        // delegates
        sfNodes.addAll(leaders);
        for (int i = 0; i < numberOfLeaders; ++i) {

            Node leader = leaders.get(i);
            Node delegate = null;
            int k = numberOfDelegates;

            while (k > 0) {
                // get random delegate
                delegate = nodes.get(random.nextInt(nodes.size()));

                double dist = getDistance(leader, delegate);

                // delegate must be outside radius
                if (dist > radius) {
                    createUndirectedEdge(graphModel, leader, delegate);

                    if (!sfNodes.contains(delegate)) {
                        sfNodes.add(delegate);
                        nodes.remove(delegate);
                    }

                    k--;
                }
            }
        }

        // apply B-A scale free algorithm
        Graph graph = graphModel.getGraph();

        for (int i = 0; i < nodes.size(); ++i) {

            // compute sum of all degrees in SF network
            int sumpk = 0;
            for (int j = 0; j < sfNodes.size(); ++j) {
                sumpk += graph.getDegree(sfNodes.get(j));
            }

            boolean success = false;
            while (!success) {

                // try to connect the new node to nodes in the network
                for (Node node : sfNodes) {
                    // degree of current sfNode
                    int pi = graph.getDegree(node);

                    // get random value
                    float p = random.nextFloat();

                    // connect if p < probability
                    if (p < 1.0 * (1 - getDistance(node, nodes.get(i)) / (2 * R)) * pi / sumpk) {
                        success = true;
                        createUndirectedEdge(graphModel, node, nodes.get(i));
                    }
                }
            }
            sfNodes.add(nodes.get(i));

            //Sleep some time
            animateEdge();
            progressTick();
        }

        sfNodes.clear();
        sfNodes = null;
        nodes.clear();
        nodes = null;
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">

    /*
     * Get euclidean distance between nodes
     */
    private double getDistance(Node n1, Node n2) {

        double x1 = n1.getNodeData().x();
        double y1 = n1.getNodeData().y();
        double x2 = n2.getNodeData().x();
        double y2 = n2.getNodeData().y();
        double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

        return dist;
    }    

    public String getName() {
        return NbBundle.getMessage(SocialGeographicGraph.class, "SocialGeographicGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(SocialGeographicGraphUI.class);
    }

    public void setNumberOfNodes(int numberOfNodes) {
        if (numberOfNodes < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfNodes = numberOfNodes;
    }

    public void setDensity(double density) {
        if (density < 0 || density > 1) {
            throw new IllegalArgumentException("Density must be between 0 and 1");
        }
        this.density = density;
    }

    public void setLinkProbability(double linkProbability) {
        if (linkProbability < 0 || linkProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.linkProbability = linkProbability;
    }

    public void setNumberOfLeaders(int numberOfLeaders) {
        if (numberOfLeaders < 0) {
            throw new IllegalArgumentException("# of leaders must be greater than 0");
        }

        this.numberOfLeaders = numberOfLeaders;
    }

    public void setNumberOfDelegates(int numberOfDelegates) {
        if (numberOfDelegates < 0) {
            throw new IllegalArgumentException("# of delegates must be greater than 0");
        }

        this.numberOfDelegates = numberOfDelegates;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public double getDensity() {
        return density;
    }

    public double getLinkProbability() {
        return linkProbability;
    }

    public int getNumberOfLeaders() {
        return numberOfLeaders;
    }

    public int getNumberOfDelegates() {
        return numberOfDelegates;
    }
    // </editor-fold>
}
