package org.gephi.ui.statistics.plugin.social;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.Sociability;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = StatisticsUI.class)
public class SociabilityUI implements StatisticsUI {

    private Sociability sociability;

    public JPanel getSettingsPanel() {
        return null;
    }

    public void setup(Statistics statistics) {
        this.sociability = (Sociability) statistics;
    }

    public void unsetup() {
        sociability = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Sociability.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(sociability.getSociability());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "SociabilityUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 300;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "SociabilityUI.shortDescription");
    }
}
