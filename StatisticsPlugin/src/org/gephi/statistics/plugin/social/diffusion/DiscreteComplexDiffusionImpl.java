/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin.social.diffusion;

import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.SNode;

/**
 *
 * @author Alexander
 *
 * Look at all friends, count their opinions, and adopt the opinion of the
 * majority <br>
 * Discrete : opinion is represented by an integer (0,1) <br>
 * Complex : look at all friends instead of a random one <br>
 */
public class DiscreteComplexDiffusionImpl extends AbstractDiffusionImpl {

    public DiscreteComplexDiffusionImpl(HierarchicalGraph graph, boolean animate, int animationDelay) {
        super(graph, animate, animationDelay);
    }

    @Override
    public float interact(SNode snode) {

        // local variables to keep track of positive & negative friends
        int npos, nneg;
        float fOpinion;

        // friends of current node                    
        NodeIterable friends = graph.getNeighbors(snode.getNode());

        nneg = 0;
        npos = 0;
        // look at all friends, count their opinions and adopt the opinion of the majority
        for (Node friend : friends) {
            SNode sfriend = new SNode(friend);
            // get friend's opinion
            fOpinion = sfriend.getValueAsFloat(SNode.Opinion);
            if (fOpinion < 0.5) {
                nneg++;
            } else {
                npos++;
            }
        }

        float myOpinion = snode.getValueAsFloat(SNode.Opinion);

        if (nneg > npos) {
            myOpinion = 0f;
        } else if (nneg < npos) {
            myOpinion = 1f;
        } else {
            myOpinion = (myOpinion < 0.5f) ? 0f : 1f;
        }

        return myOpinion;
    }
}