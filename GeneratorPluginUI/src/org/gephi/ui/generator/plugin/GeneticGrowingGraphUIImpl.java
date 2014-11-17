package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.genetic.GeneticGrowingGraph;
import org.gephi.io.generator.plugin.genetic.GeneticGrowingGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = GeneticGrowingGraphUI.class)
public class GeneticGrowingGraphUIImpl implements GeneticGrowingGraphUI {

    private GeneticGrowingGraphPanel panel;
    private GeneticGrowingGraph sfGraph;

    public GeneticGrowingGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new GeneticGrowingGraphPanel();
        }
        return GeneticGrowingGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.sfGraph = (GeneticGrowingGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new GeneticGrowingGraphPanel();
        }
        // network params
        panel.sizeField.setText(String.valueOf(sfGraph.getNumberOfNodes()));
        panel.edgesField.setText(String.valueOf(sfGraph.getAvgDeg()));
        panel.communityField.setText(String.valueOf(sfGraph.getNumberOfCommunities()));      
        // animation
        panel.animateCheckBox.setSelected(sfGraph.getAnimate());
        panel.tAnimateNode.setText("" + sfGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText("" + sfGraph.getAnimationEdgeDelay());
    }

    public void unsetup() {
        // network params
        sfGraph.setNumberOfNodes(Integer.parseInt(panel.sizeField.getText()));
        sfGraph.setAvgDeg(Integer.parseInt(panel.edgesField.getText()));
        sfGraph.setNumberOfCommunities(Integer.parseInt(panel.communityField.getText()));
        // animation
        sfGraph.setAnimate(panel.animateCheckBox.isSelected());
        sfGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        sfGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));

        panel = null;
    }
}
