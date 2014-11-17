package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.SocialGeographicGraph;
import org.gephi.io.generator.plugin.SocialGeographicGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = SocialGeographicGraphUI.class)
public class SocialGeographicGraphUIImpl implements SocialGeographicGraphUI {

    private SocialGeographicGraphPanel panel;
    private SocialGeographicGraph socialGraph;

    public SocialGeographicGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new SocialGeographicGraphPanel();
        }
        return SocialGeographicGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.socialGraph = (SocialGeographicGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new SocialGeographicGraphPanel();
        }
        panel.nField.setText(String.valueOf(socialGraph.getNumberOfNodes()));
        panel.linkPField.setText(String.valueOf(socialGraph.getLinkProbability()));
        panel.densityField.setText(String.valueOf(socialGraph.getDensity()));
        panel.nlField.setText(String.valueOf(socialGraph.getNumberOfLeaders()));
        panel.ndField.setText(String.valueOf(socialGraph.getNumberOfDelegates()));

        panel.animateCheckBox.setSelected(socialGraph.getAnimate());
        panel.tAnimateNode.setText(String.valueOf(socialGraph.getAnimationNodeDelay()));
        panel.tAnimateEdge.setText(String.valueOf(socialGraph.getAnimationEdgeDelay()));
    }

    public void unsetup() {
        //Set params
        socialGraph.setNumberOfNodes(Integer.parseInt(panel.nField.getText()));
        socialGraph.setLinkProbability(Double.parseDouble(panel.linkPField.getText()));
        socialGraph.setDensity(Double.parseDouble(panel.densityField.getText()));
        socialGraph.setNumberOfLeaders(Integer.parseInt(panel.nlField.getText()));
        socialGraph.setNumberOfDelegates(Integer.parseInt(panel.ndField.getText()));

        socialGraph.setAnimate(panel.animateCheckBox.isSelected());
        socialGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        socialGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));

        panel = null;
    }
}
