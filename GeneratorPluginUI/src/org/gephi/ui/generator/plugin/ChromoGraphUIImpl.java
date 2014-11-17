package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.genetic.ChromoGraph;
import org.gephi.io.generator.plugin.genetic.ChromoGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = ChromoGraphUI.class)
public class ChromoGraphUIImpl implements ChromoGraphUI {

    private ChromoGraphPanel panel;
    private ChromoGraph chromoGraph;

    public ChromoGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new ChromoGraphPanel();
        }
        return ChromoGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.chromoGraph = (ChromoGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new ChromoGraphPanel();
        }
        panel.nodeField.setText(String.valueOf(chromoGraph.getNumberOfNodes()));
        panel.radiusField.setText(String.valueOf(chromoGraph.getRadius()));
        panel.linkField.setText(String.valueOf(chromoGraph.getLinkProbability()));
        panel.stepsField.setText(String.valueOf(chromoGraph.getgSteps()));
        panel.fitnessField.setText(String.valueOf(chromoGraph.getFitnessThreshold()));
        panel.solutionField.setText(String.valueOf(chromoGraph.getSolution()));

        panel.animateCheckBox.setSelected(chromoGraph.getAnimate());
        panel.tAnimateNode.setText(String.valueOf(chromoGraph.getAnimationNodeDelay()));
        panel.tAnimateEdge.setText(String.valueOf(chromoGraph.getAnimationEdgeDelay()));
    }

    public void unsetup() {
        //Set params
        chromoGraph.setNumberOfNodes(Integer.parseInt(panel.nodeField.getText()));
        chromoGraph.setRadius(Integer.parseInt(panel.radiusField.getText()));
        chromoGraph.setLinkProbability(Double.parseDouble(panel.linkField.getText()));
        chromoGraph.setGSteps(Integer.parseInt(panel.stepsField.getText()));
        chromoGraph.setFitnessThreshold(Double.parseDouble(panel.fitnessField.getText()));
        chromoGraph.setSolution(panel.solutionField.getText());        

        chromoGraph.setAnimate(panel.animateCheckBox.isSelected());
        chromoGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        chromoGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));

        panel = null;
    }
}
