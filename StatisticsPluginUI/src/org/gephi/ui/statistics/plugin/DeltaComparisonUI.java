package org.gephi.ui.statistics.plugin;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.DeltaComparison;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.statistics.spi.StatisticsUICallbackProvider;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class DeltaComparisonUI implements StatisticsUI, StatisticsUICallbackProvider {

    private DeltaComparisonPanel panel;
    private DeltaComparison delta;

    public JPanel getSettingsPanel() {
        panel = new DeltaComparisonPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        if (panel != null) {
            delta = (DeltaComparison) statistics;
            panel.setup();
            panel.setEnabledMetrics(delta.getEnabledMetrics());
        }
    }

    public void unsetup() {

        if (panel != null) {
            panel.unsetup(delta);

            if (panel.checkSave.isSelected()) {
                delta.setStatisticsUIProvider(this);
            }
            else
            {
                delta.setStatisticsUIProvider(null);
            }
        }
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return DeltaComparison.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(delta.getDeltaArithmetic());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DeltaComparisonUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 1200;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "DeltaComparisonUI.shortDescription");
    }

    /**
     * Updates the base measurements after the delta metric has finished
     * measuring.
     *
     * @param objects - list of measurements
     */
    public void callback(Object[] objects) {
        DeltaComparisonPanel panel = new DeltaComparisonPanel();

        Double[] values = new Double[objects.length];

        for (int i = 0; i < objects.length; ++i) {
            if (objects[i] != null) {
                values[i] = (Double) objects[i];
            } else {
                values[i] = 0.0;
            }
        }

        panel.unsetup(values);
    }
}
