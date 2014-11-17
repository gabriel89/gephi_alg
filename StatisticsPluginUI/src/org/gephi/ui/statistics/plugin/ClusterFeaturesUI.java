package org.gephi.ui.statistics.plugin;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.ClusterFeatures;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class ClusterFeaturesUI implements StatisticsUI {
   
    //private ModularityPanel panel;
    private ClusterFeatures mod;

    public JPanel getSettingsPanel() {
        return null;
//        panel = new ModularityPanel();
//        return panel;
    }

    public void setup(Statistics statistics) {
        this.mod = (ClusterFeatures) statistics;
//        if (panel != null) {           
//            panel.setRandomize(mod.getRandom());
//            panel.setUseWeight(mod.getUseWeight());
//            panel.setResolution(mod.getResolution());
//        }
    }

    public void unsetup() {
//        if (panel != null) {
//            mod.setRandom(panel.isRandomize());
//            mod.setUseWeight(panel.useWeight());
//            mod.setResolution(panel.resolution());
//            settings.save(mod);
//        }
//        mod = null;
//        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ClusterFeatures.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(mod.getPopulation());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "ClusterFeaturesUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 1100;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "ClusterFeaturesUI.shortDescription");
    }   
}
