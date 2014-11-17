package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.StaticGeographicGraph;
import org.gephi.io.generator.plugin.StaticGeographicGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StaticGeographicGraphUI.class)
public class StaticGeographicGraphUIImpl implements StaticGeographicGraphUI {

    private StaticGeographicGraphPanel panel;
    private StaticGeographicGraph geoGraph;

    public StaticGeographicGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new StaticGeographicGraphPanel();
        }
        return StaticGeographicGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.geoGraph = (StaticGeographicGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new StaticGeographicGraphPanel();
        }
        panel.cellField.setText(String.valueOf(geoGraph.getNumberOfNodes()));
        panel.maxDistField.setText(String.valueOf(geoGraph.getMaxLinkDistance()));
        panel.socialField.setText(String.valueOf(geoGraph.getLinkRequestProbability()));
        panel.paranoidField.setText(String.valueOf(geoGraph.getLinkAcceptProbability()));
        
        panel.animateCheckBox.setSelected(geoGraph.getAnimate());
        panel.tAnimateNode.setText(""+geoGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText(""+geoGraph.getAnimationEdgeDelay());
    }

    public void unsetup() {
        //Set params
        geoGraph.setNumberOfNodes(Integer.parseInt(panel.cellField.getText()));
        geoGraph.setMaxLinkDistance(Double.parseDouble(panel.maxDistField.getText()));
        geoGraph.setLinkRequestProbability(Double.parseDouble(panel.socialField.getText()));
        geoGraph.setLinkAcceptProbability(Double.parseDouble(panel.paranoidField.getText()));
        
        geoGraph.setAnimate(panel.animateCheckBox.isSelected());
        geoGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        geoGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        
        panel = null;
    }
}
