/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin.social.diffusion;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.SEdge;
import org.gephi.graph.api.SNode;

/**
 *
 * @author Alexander
 *
 * Look at all friends, count their opinions, and adopt the opinion of the
 * majority <br> Continuous : opinion is represented by a float [0,1] <br>
 * Complex : look at the majority of neighbors, all with a unique trust factor
 * Tij <br>
 */
public class ContinuousComplexDiffusionImpl extends AbstractDiffusionImpl {

    public ContinuousComplexDiffusionImpl(HierarchicalGraph graph, boolean animate, int animationDelay) {
        super(graph, animate, animationDelay);
    }

    @Override
    public float interact(SNode snode) {

        // local variables
        float myOpinion, fOpinion, trust;        

        myOpinion = snode.getValueAsFloat(SNode.Opinion);
        SNode friend;
        for (Edge edge : graph.getEdges(snode.getNode())) {

            // get edge trust
            trust = new SEdge(edge).getValueAsFloat(SEdge.Trust);
            // get friend
            if (snode.getNode().equals(edge.getSource())) {
                friend = new SNode(edge.getTarget());
            } else {
                friend = new SNode(edge.getSource());
            }
            fOpinion = friend.getValueAsFloat(SNode.Opinion);

            // update opinion after each interaction
            myOpinion = trust * fOpinion + (1 - trust) * myOpinion;
        }


        friend = null;
        return myOpinion;
    }
}