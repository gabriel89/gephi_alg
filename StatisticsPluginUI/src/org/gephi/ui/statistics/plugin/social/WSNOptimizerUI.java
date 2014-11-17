package org.gephi.ui.statistics.plugin.social;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.WSNOptimizer;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class WSNOptimizerUI implements StatisticsUI {

    private WSNOptimizerPanel panel;
    private WSNOptimizer wsn;

    public JPanel getSettingsPanel() {
        if (panel == null) {
            panel = new WSNOptimizerPanel();
        }
        return WSNOptimizerPanel.createValidationPanel(panel);
    }

    public void setup(Statistics statistics) {
        this.wsn = (WSNOptimizer) statistics;
        if (panel != null) {
            panel.extraEdgesField.setText(String.valueOf(wsn.getNExtraEdges()));
            panel.resolutionField.setText(String.valueOf(wsn.getResolution()));
            panel.wifiRadiusField.setText(String.valueOf(wsn.getWifiRadius()));
            panel.growthBox.setSelected(wsn.getGrow());
            panel.radiusRatioField.setText(String.valueOf(wsn.getRatio()));
            panel.setDirected(wsn.getDirected());

            panel.animateCheckBox.setSelected(wsn.getAnimate());
            panel.tAnimateNode.setText(String.valueOf(wsn.getAnimationNodeDelay()));
            panel.tAnimateEdge.setText(String.valueOf(wsn.getAnimationEdgeDelay()));
        }
    }

    public void unsetup() {
        if (panel != null) {
            wsn.setNExtraEdges(Integer.parseInt(panel.extraEdgesField.getText()));
            wsn.setResolution(Double.parseDouble(panel.resolutionField.getText()));
            wsn.setWifiRadius(Integer.parseInt(panel.wifiRadiusField.getText()));
            wsn.setGrow(panel.growthBox.isSelected());
            wsn.setRatio(Integer.parseInt(panel.radiusRatioField.getText()));
            wsn.setDirected(panel.isDirected());

            wsn.setAnimate(panel.animateCheckBox.isSelected());
            wsn.setAnimationNodeDelay(Integer.parseInt(panel.tAnimateNode.getText()));
            wsn.setAnimationEdgeDelay(Integer.parseInt(panel.tAnimateEdge.getText()));
        }
        wsn = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return WSNOptimizer.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(wsn.getRatio());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "WSNOptimizerUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_SOCIAL;
    }

    public int getPosition() {
        return 400;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "WSNOptimizerUI.shortDescription");
    }
}
