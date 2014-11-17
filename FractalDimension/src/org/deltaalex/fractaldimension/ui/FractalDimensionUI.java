package org.deltaalex.fractaldimension.ui;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.deltaalex.fractaldimension.plugin.FractalDimension;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class FractalDimensionUI implements StatisticsUI {
   
    private FractalDimensionPanel panel;
    private FractalDimension dim;

    public JPanel getSettingsPanel() {
        panel = new FractalDimensionPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.dim = (FractalDimension) statistics;
        if (panel != null) {                       
            panel.setMinBoxes(dim.getMinBoxes());
            panel.setMaxBoxes(dim.getMaxBoxes());
            panel.setCheckCores(dim.getCheckCores());
            panel.setCores(dim.getCores());
        }
    }

    public void unsetup() {
        if (panel != null) {
            dim.setDirected(panel.isDirected());
            dim.setMinBoxes(panel.getMinBoxes());
            dim.setMaxBoxes(panel.getMaxBoxes());
            dim.setCheckCores(panel.getCheckCores());
            dim.setCores(panel.getCores());                      
        }
        dim = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return FractalDimension.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(dim.getDimension());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "FractalDimensionUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 1000;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "FractalDimensionUI.shortDescription");
    }    
}
