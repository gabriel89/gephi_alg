package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.WSDDGraph;
import org.gephi.io.generator.plugin.WSDDGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = WSDDGraphUI.class)
public class WSDDGraphUIImpl implements WSDDGraphUI {

    private WSDDGraphPanel panel;
    private WSDDGraph wsddGraph;

    public WSDDGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new WSDDGraphPanel();
        }
        return WSDDGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.wsddGraph = (WSDDGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new WSDDGraphPanel();
        }
        panel.cellField.setText(String.valueOf(wsddGraph.getNumberOfCells()));     
        panel.cellSizeField.setText(String.valueOf(wsddGraph.getAvgCellSize()));     
        panel.kField.setText(String.valueOf(wsddGraph.getKNeighbors()));  
        panel.wiringField.setText(String.valueOf(wsddGraph.getWiringProbability()));  
        
        panel.animateCheckBox.setSelected(wsddGraph.getAnimate());
        panel.tAnimateNode.setText(""+wsddGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText(""+wsddGraph.getAnimationEdgeDelay());
    }

    public void unsetup() {
        //Set params
        wsddGraph.setNumberOfCells(Integer.parseInt(panel.cellField.getText()));       
        wsddGraph.setAvgCellSize(Integer.parseInt(panel.cellSizeField.getText()));       
        wsddGraph.setKNeighbors(Integer.parseInt(panel.kField.getText()));   
        wsddGraph.setWiringProbability(Double.parseDouble(panel.wiringField.getText()));   
        
        wsddGraph.setAnimate(panel.animateCheckBox.isSelected());
        wsddGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        wsddGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        
        panel = null;
    }
}
