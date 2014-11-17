package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.ScaleFreeGraph;
import org.gephi.io.generator.plugin.ScaleFreeGraph.Metric;
import org.gephi.io.generator.plugin.ScaleFreeGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = ScaleFreeGraphUI.class)
public class ScaleFreeGraphUIImpl implements ScaleFreeGraphUI {
    
    private ScaleFreeGraphPanel panel;
    private ScaleFreeGraph sfGraph;
    
    public ScaleFreeGraphUIImpl() {
    }
    
    public JPanel getPanel() {
        if (panel == null) {
            panel = new ScaleFreeGraphPanel();
        }
        return ScaleFreeGraphPanel.createValidationPanel(panel);
    }
    
    public void setup(Generator generator) {
        this.sfGraph = (ScaleFreeGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new ScaleFreeGraphPanel();
        }
        // network params
        panel.cellField.setText(String.valueOf(sfGraph.getNumberOfNodes()));        
        panel.seedSizeField.setText(String.valueOf(sfGraph.getSeedSize()));
        panel.seedWiringField.setText(String.valueOf(sfGraph.getSeedWiring()));
        panel.fractalSizeField.setText(String.valueOf(sfGraph.getFractalSize()));
        // weights radio group
        panel.radioNoWeights.setSelected(!sfGraph.getWeighted());
        panel.radioNormalWeights.setSelected(sfGraph.getWeighted() && !sfGraph.getPowerLawWeights());
        panel.radioPowerWeights.setSelected(sfGraph.getWeighted() && sfGraph.getPowerLawWeights());
        // metrics check group
        panel.checkDegree.setSelected(sfGraph.getMetric(Metric.Degree));
        panel.checkBetweenness.setSelected(sfGraph.getMetric(Metric.Betweenness));
        panel.checkCentrality.setSelected(sfGraph.getMetric(Metric.Centrality));
        panel.checkCloseness.setSelected(sfGraph.getMetric(Metric.Closeness));
        panel.checkClustering.setSelected(sfGraph.getMetric(Metric.Clustering));
        // animation
        panel.animateCheckBox.setSelected(sfGraph.getAnimate());
        panel.tAnimateNode.setText("" + sfGraph.getAnimationNodeDelay());
        panel.tAnimateEdge.setText("" + sfGraph.getAnimationEdgeDelay());
    }
    
    public void unsetup() {
        // network params
        sfGraph.setNumberOfNodes(Integer.parseInt(panel.cellField.getText()));        
        sfGraph.setSeedSize(Integer.parseInt(panel.seedSizeField.getText()));
        sfGraph.setSeedWiring(Double.parseDouble(panel.seedWiringField.getText()));
        sfGraph.setFractalSize(Integer.parseInt(panel.fractalSizeField.getText()));
        // weights radio group
        sfGraph.setWeighted(!panel.radioNoWeights.isSelected());
        sfGraph.setPowerLawWeights(panel.radioPowerWeights.isSelected());
        // metric check group 
        sfGraph.setMetrics(new Metric[]{
                    panel.checkDegree.isSelected() ? Metric.Degree : null,
                    panel.checkBetweenness.isSelected() ? Metric.Betweenness : null,
                    panel.checkCentrality.isSelected() ? Metric.Centrality : null,
                    panel.checkCloseness.isSelected() ? Metric.Closeness : null,
                    panel.checkClustering.isSelected() ? Metric.Clustering : null,});

        // animation
        sfGraph.setAnimate(panel.animateCheckBox.isSelected());
        sfGraph.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
        sfGraph.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        
        panel = null;
    }
}
