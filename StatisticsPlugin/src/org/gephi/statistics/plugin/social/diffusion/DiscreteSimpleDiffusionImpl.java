/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin.social.diffusion;

import java.util.Random;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.SNode;

/**
 *
 * @author Alexander
 *
 * Look at one random friend at a time and take his opinion <br>
 * Discrete : opinion is represented by an integer (0,1) <br>
 * Simple : look at a single random friend <br>
 */
public class DiscreteSimpleDiffusionImpl extends AbstractDiffusionImpl {
  
    public DiscreteSimpleDiffusionImpl(HierarchicalGraph graph, boolean animate, int animationDelay) {
        super(graph, animate, animationDelay);
    }

    @Override
    public float interact(SNode snode) {

        // local variables
        float fOpinion;

        // friends of current node                    
        NodeIterable friends = graph.getNeighbors(snode.getNode());
        // select one random friend
        Node friend = selectRandom(friends.iterator(), random);
        if (friend != null) {
            // get friend's opinion
            fOpinion = new SNode(friend).getValueAsFloat(SNode.Opinion);
        } else {
            // if no friends - retain opinion
            return snode.getValueAsFloat(SNode.Opinion);
        }

        return fOpinion < 0.5f ? 0f : 1f;
    }
}