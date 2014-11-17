package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.FacebookGraph;
import org.gephi.io.generator.plugin.FacebookGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = FacebookGraphUI.class)
public class FacebookGraphUIImpl implements FacebookGraphUI {

    private FacebookGraphPanel panel;
    private FacebookGraph facebookGraph;

    public FacebookGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new FacebookGraphPanel();
        }
        return FacebookGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.facebookGraph = (FacebookGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new FacebookGraphPanel();
        }
        panel.cellField.setText(String.valueOf(facebookGraph.getNumberOfCells()));
        panel.intraEdgeField.setText(String.valueOf(facebookGraph.getIntraWiringProbability()));
        panel.interEdgeField.setText(String.valueOf(facebookGraph.getInterWiringProbability()));
        panel.cellSizeField.setText(String.valueOf(facebookGraph.getAvgCellSize()));
        
        panel.animateCheckBox.setSelected(facebookGraph.getAnimate());
        panel.tAnimateNode.setText(""+facebookGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText(""+facebookGraph.getAnimationEdgeDelay());
    }

    public void unsetup() {
        //Set params
        facebookGraph.setNumberOfCells(Integer.parseInt(panel.cellField.getText()));
        facebookGraph.setIntraWiringProbability(Double.parseDouble(panel.intraEdgeField.getText()));
        facebookGraph.setInterWiringProbability(Double.parseDouble(panel.interEdgeField.getText()));
        facebookGraph.setAvgCellSize(Integer.parseInt(panel.cellSizeField.getText()));
        
        facebookGraph.setAnimate(panel.animateCheckBox.isSelected());
        facebookGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        facebookGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        
        panel = null;
    }
}
