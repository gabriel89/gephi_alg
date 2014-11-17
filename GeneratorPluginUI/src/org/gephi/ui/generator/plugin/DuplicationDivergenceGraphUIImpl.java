package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.DuplicationDivergenceGraph;
import org.gephi.io.generator.plugin.DuplicationDivergenceGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = DuplicationDivergenceGraphUI.class)
public class DuplicationDivergenceGraphUIImpl implements DuplicationDivergenceGraphUI {

    private DuplicationDivergenceGraphPanel panel;
    private DuplicationDivergenceGraph ddGraph;

    public DuplicationDivergenceGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new DuplicationDivergenceGraphPanel();
        }
        return DuplicationDivergenceGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.ddGraph = (DuplicationDivergenceGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new DuplicationDivergenceGraphPanel();
        }
        panel.cellField.setText(String.valueOf(ddGraph.getNumberOfNodes()));     
        panel.retentionField.setText(String.valueOf(ddGraph.getRetention()));     
        
        panel.animateCheckBox.setSelected(ddGraph.getAnimate());
        panel.tAnimateNode.setText(""+ddGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText(""+ddGraph.getAnimationEdgeDelay());
    }

    public void unsetup() {
        //Set params
        ddGraph.setNumberOfNodes(Integer.parseInt(panel.cellField.getText()));       
        ddGraph.setRetention(Double.parseDouble(panel.retentionField.getText()));       
        
        ddGraph.setAnimate(panel.animateCheckBox.isSelected());
        ddGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        ddGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        
        panel = null;
    }
}
