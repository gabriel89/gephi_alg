package org.gephi.statistics.plugin.social;

import org.gephi.graph.api.HierarchicalGraph;

/**
 * Saves data between runs
 *
 * @author Alexander
 */
class SavedState {

    static int w = 15, h = 15;
    
    static HierarchicalGraph originalGraph = null;
    
    static boolean[]neighborLinks = new boolean[] {
        true,  true,  true,
        true,  false, true,
        false, false, false};
}
