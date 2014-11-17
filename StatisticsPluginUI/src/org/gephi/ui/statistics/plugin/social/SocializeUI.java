package org.gephi.ui.statistics.plugin.social;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.social.Socialize;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsUI.class)
public class SocializeUI implements StatisticsUI {

    //private final StatSettings settings = new StatSettings();
    private SocializePanel panel;
    private Socialize opinion;

    public JPanel getSettingsPanel() {
        if(panel == null)
        {
        panel = new SocializePanel();
        }        
        return SocializePanel.createValidationPanel(panel);
    }

     public void setup(Statistics statistics) {
        this.opinion = (Socialize) statistics;
        if (panel != null) {            
            panel.bKeepColor.setSelected(opinion.getKeepColor());            
            panel.bDontKeepColor.setSelected(!opinion.getKeepColor());                        
        }
    }

    public void unsetup() {
        if (panel != null) {                        
            opinion.setKeepColor(panel.bKeepColor.isSelected());                           
        }
        opinion = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Socialize.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(opinion.getPopulation());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "SocializeUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_SOCIAL;
    }

    public int getPosition() {
        return 1;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "SocializeUI.shortDescription");
    }       
}
