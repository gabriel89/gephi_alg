package org.gephi.io.generator.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.gephi.graph.api.*;
import org.gephi.io.generator.plugin.ScaleFreeGraph.Metric;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates a tunable growing network model as defined by Zaidi et al.
 *
 * @ SCA 2013:
 * http://www.google.ro/url?sa=t&rct=j&q=&esrc=s&frm=1&source=web&cd=1&cad=rja&ved=0CCsQFjAA&url=http%3A%2F%2Fhal.archives-ouvertes.fr%2Fdocs%2F00%2F87%2F78%2F64%2FPDF%2FUploadedFile_130276101104109828.pdf&ei=ld2NUuzuKoPTtAazxIGYCQ&usg=AFQjCNEPf4X_4EHcYqXIjZcIsEatGBAY7g&sig2=hSIUVRctldifBJ5BLNHjTg&bvm=bv.56987063,d.Yms
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = Generator.class)
public class TunableGrowingGraph extends AbstractGraph implements Generator {

    private int numberOfNodes = 200;
    private int numberOfEdges = 8;
    private int numberOfCommunities = 4;
    private double pTriad = 0.6;
    private double pInterCom = 0.9;
    // todo
    private Metric[] metrics = {Metric.Betweenness};
    private boolean weighted = false;
    private boolean powerLawWeights = true;

    @Override
    protected int initialize() {
        return numberOfNodes;
    }

    @Override
    protected void runGeneration(GraphModel graphModel, Random random) {
        // nodes
        Cell[] cells = new Cell[numberOfCommunities];
        Graph graph = graphModel.getGraph();
        int size = 0;

        progress.switchToIndeterminate();

        // initialize each cell
        for (int cell = 0; cell < numberOfCommunities; ++cell) {
            cells[cell] = new Cell();

            // create triad
            Node n1 = createNode(graphModel, size++, true);
            Node n2 = createNode(graphModel, size++, true);
            Node n3 = createNode(graphModel, size++, true);
            // add to community
            cells[cell].addNode(n1);
            cells[cell].addNode(n2);
            cells[cell].addNode(n3);
            // create triad edges
            createUndirectedEdge(graphModel, n1, n2);
            createUndirectedEdge(graphModel, n1, n3);
            createUndirectedEdge(graphModel, n2, n3);
        }

        // connect all communities
        for (int i = 0; i < numberOfCommunities - 1; ++i) {
            for (int j = i + 1; j < numberOfCommunities; ++j) {
                // pick random nodes
                Node n1 = cells[i].getNodes().get(random.nextInt(cells[i].getSize()));
                Node n2 = cells[j].getNodes().get(random.nextInt(cells[j].getSize()));

                createUndirectedEdge(graphModel, n1, n2);
            }
        }

        // network growth - two step process
        Cell previousCell = null;

        for (int i = size; i < numberOfNodes; ++i) {

            // create & add first node
            Node currentNode = createNode(graphModel, size++, true);
            Node targetNode = null;
            Cell currentCell = null;

            // compute sum of fitness metric (e.g. degree) in network
            double sumpk = 0.0;
            for (Cell cell : cells) {
                for (Node node : cell.getNodes()) {
                    sumpk += graph.getDegree(node);
                }
            }

            // try to add the new ndoe to the network
            boolean success = false;
            while (!success) {

                // try to connect the new node to nodes in the network
                for (Cell cell : cells) {
                    for (Node node : cell.getNodes().toArray(new Node[]{})) {

                        // target ndoe fitness
                        double pi = graph.getDegree(node) / sumpk;

                        // get random value
                        double p = random.nextDouble();

                        // connect if p < probability
                        if (p < pi) {
                            Edge edge = createUndirectedEdge(graphModel, currentNode, node);
//                        if (weighted) {
//                            double weight = powerLawWeights ? getPowerDistributedDoubleValue(random, 0, 1) : random.nextFloat();
//                            edge.setWeight((float) weight);
//                        }                                          
                            success = true;
                            cell.addNode(currentNode);
                            currentCell = cell;
                            targetNode = node;
                        }
                    }
                }
            }

            // new connect the node to an additional edges-1 friends of the target node
            // if cell size is smaller than required degree then connect to all friends of the target node
            List<Node> friends = filterByCommunity(graph.getNeighbors(targetNode).toArray(), currentCell);
            int newEdges = Math.min(numberOfEdges - 1, friends.size() - 1);

            boolean tryConnect = true;
            for (int e = 0; e < newEdges && tryConnect; ++e) {
                //while(newEdges > 0 && tryConnect) {

                // try to connect to friends of target                
                while (tryConnect) {
                    Node friend = friends.get(random.nextInt(friends.size()));

                    // if no edge exists yet
                    if (!currentNode.equals(friend) && existsNoEdge(graphModel, friend, currentNode)) {
                        double p = random.nextDouble();
                        if (p < pTriad) {
                            createUndirectedEdge(graphModel, currentNode, friend);
                        }
                        //newEdges--;
                        break;
                    }

                    // hack: check for possible links                    
                    tryConnect = false;
                    for (Node f : friends) {
                        if (!currentNode.equals(f)) {
                            if (existsNoEdge(graphModel, f, currentNode)) {
                                tryConnect = true;
                            }
                        }
                    }
                }
            }

            // connect communities of last two new nodes
            double p = random.nextDouble();

            if (p < pInterCom) {

                if (i % 2 == 0 && previousCell != null) {
                    Edge newEdge = null;
                    while (newEdge == null) {
                        Node n1 = previousCell.getNodes().get(random.nextInt(previousCell.getSize()));
                        Node n2 = currentCell.getNodes().get(random.nextInt(currentCell.getSize()));

                        newEdge = createUndirectedEdge(graphModel, n1, n2);
                    }
                }
            }

            previousCell = currentCell;
        }

        progress.switchToDeterminate(100);
    }

    private List<Node> filterByCommunity(Node[] neighbors, Cell cell) {
        List<Node> filtered = new ArrayList<Node>();

        for (Node neighbor : neighbors) {
            if (cell.getNodes().contains(neighbor)) {
                filtered.add(neighbor);
            }
        }

        return filtered;
    }

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getName() {
        return NbBundle.getMessage(TunableGrowingGraph.class, "TunableGrowingGraph.name");
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(TunableGrowingGraphUI.class);
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public void setNumberOfEdges(int numberOfEdges) {
        this.numberOfEdges = numberOfEdges;
    }

    public void setNumberOfCommunities(int numberOfCommunities) {
        this.numberOfCommunities = numberOfCommunities;
    }

    public void setpTriad(double pTriad) {
        this.pTriad = pTriad;
    }

    public void setpInterCom(double pInterCom) {
        this.pInterCom = pInterCom;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getNumberOfEdges() {
        return numberOfEdges;
    }

    public int getNumberOfCommunities() {
        return numberOfCommunities;
    }

    public double getpTriad() {
        return pTriad;
    }

    public double getpInterCom() {
        return pInterCom;
    }
    // </editor-fold>   
}
