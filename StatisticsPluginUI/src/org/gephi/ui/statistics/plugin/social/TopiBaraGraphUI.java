package org.gephi.ui.statistics.plugin.social;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.social.TopiBaraGraph;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsUI.class)
public class TopiBaraGraphUI implements StatisticsUI {

    private TopiBaraGraphPanel panel;
    private TopiBaraGraph exam;

    public JPanel getSettingsPanel() {
        if (panel == null) {
            panel = new TopiBaraGraphPanel();
        }
        return TopiBaraGraphPanel.createValidationPanel(panel);
    }

    public void setup(Statistics statistics) {
        this.exam = (TopiBaraGraph) statistics;

        if (panel != null) {
            panel.sizeField.setText(String.valueOf(exam.getMaxSize()));
            panel.growthField.setText(String.valueOf(exam.getGrowthFactor()));
            panel.avgDegField.setText(String.valueOf(exam.getAvgDegree()));

            panel.animateCheckBox.setSelected(exam.getAnimate());
            panel.tAnimateNode.setText(String.valueOf(exam.getAnimationNodeDelay()));
            panel.tAnimateEdge.setText(String.valueOf(exam.getAnimationEdgeDelay()));
        }
    }

    public void unsetup() {
        if (panel != null) {
            exam.setMaxSize(Integer.parseInt(panel.sizeField.getText()));
            exam.setGrowthFactor(Double.parseDouble(panel.growthField.getText()));
            exam.setAvgDegree(Double.parseDouble(panel.avgDegField.getText()));

            exam.setAnimate(panel.animateCheckBox.isSelected());
            exam.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
            exam.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        }
        exam = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return TopiBaraGraph.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(exam.getMaxSize());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "TopiBaraGraphUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_SOCIAL;
    }

    public int getPosition() {
        return 500;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "TopiBaraGraphUI.shortDescription");
    }
}
