package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.ScaleFreeGraph.Metric;
import org.gephi.io.generator.plugin.TunableGrowingGraph;
import org.gephi.io.generator.plugin.TunableGrowingGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = TunableGrowingGraphUI.class)
public class TunableGrowingGraphUIImpl implements TunableGrowingGraphUI {

    private TunableGrowingGraphPanel panel;
    private TunableGrowingGraph sfGraph;

    public TunableGrowingGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new TunableGrowingGraphPanel();
        }
        return TunableGrowingGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.sfGraph = (TunableGrowingGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new TunableGrowingGraphPanel();
        }
        // network params
        panel.sizeField.setText(String.valueOf(sfGraph.getNumberOfNodes()));
        panel.edgesField.setText(String.valueOf(sfGraph.getNumberOfEdges()));
        panel.communityField.setText(String.valueOf(sfGraph.getNumberOfCommunities()));
        panel.triadField.setText(String.valueOf(sfGraph.getpTriad()));
        panel.interComField.setText(String.valueOf(sfGraph.getpInterCom()));
        // animation
        panel.animateCheckBox.setSelected(sfGraph.getAnimate());
        panel.tAnimateNode.setText("" + sfGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText("" + sfGraph.getAnimationEdgeDelay());
    }

    public void unsetup() {
        // network params
        sfGraph.setNumberOfNodes(Integer.parseInt(panel.sizeField.getText()));
        sfGraph.setNumberOfEdges(Integer.parseInt(panel.edgesField.getText()));
        sfGraph.setNumberOfCommunities(Integer.parseInt(panel.communityField.getText()));
        sfGraph.setpTriad(Double.parseDouble(panel.triadField.getText()));
        sfGraph.setpInterCom(Double.parseDouble(panel.interComField.getText()));
        // animation
        sfGraph.setAnimate(panel.animateCheckBox.isSelected());
        sfGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        sfGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));

        panel = null;
    }
}
