package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.genetic.GeneticGraph;
import org.gephi.io.generator.plugin.genetic.GeneticGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = GeneticGraphUI.class)
public class GeneticGraphUIImpl implements GeneticGraphUI {

    private GeneticGraphPanel panel;
    private GeneticGraph geneticGraph;

    public GeneticGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new GeneticGraphPanel();
        }
        return GeneticGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.geneticGraph = (GeneticGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new GeneticGraphPanel();
        }
        panel.cellField.setText(String.valueOf(geneticGraph.getNumberOfCells()));
        panel.intraEdgeField.setText(String.valueOf(geneticGraph.getIntraWiringProbability()));
        panel.interEdgeField.setText(String.valueOf(geneticGraph.getInterWiringProbability()));
        panel.cellSizeField.setText(String.valueOf(geneticGraph.getAvgCellSize()));
        
        panel.animateCheckBox.setSelected(geneticGraph.getAnimate());
        panel.tAnimateNode.setText(""+geneticGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText(""+geneticGraph.getAnimationEdgeDelay());
    }

    public void unsetup() {
        //Set params
        geneticGraph.setNumberOfCells(Integer.parseInt(panel.cellField.getText()));
        geneticGraph.setIntraWiringProbability(Double.parseDouble(panel.intraEdgeField.getText()));
        geneticGraph.setInterWiringProbability(Double.parseDouble(panel.interEdgeField.getText()));
        geneticGraph.setAvgCellSize(Integer.parseInt(panel.cellSizeField.getText()));
        
        geneticGraph.setAnimate(panel.animateCheckBox.isSelected());
        geneticGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        geneticGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        
        panel = null;
    }
}
