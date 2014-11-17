package org.gephi.ui.statistics.plugin.social;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.social.Diffusion;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsUI.class)
public class DiffusionUI implements StatisticsUI {

    //private final StatSettings settings = new StatSettings();
    private DiffusionPanel panel;
    private Diffusion opinion;

    public JPanel getSettingsPanel() {
        if(panel == null)
        {
        panel = new DiffusionPanel();
        }        
        return DiffusionPanel.createValidationPanel(panel);
    }

     public void setup(Statistics statistics) {
        this.opinion = (Diffusion) statistics;
        if (panel != null) {
            //settings.load(opinion);            
            panel.selectModel(opinion.getDiffuse());                        
            panel.diffAnimateCheckBox.setSelected(opinion.getAnimate());
            panel.tAnimation.setText(""+opinion.getAnimationDelay());
        }
    }

    public void unsetup() {
        if (panel != null) {            
            opinion.setDiffuse(panel.getSelectedModel());                        
            opinion.setAnimate(panel.diffAnimateCheckBox.isSelected());
            opinion.setAnimationDelay(Integer.parseInt(panel.tAnimation.getText().trim()));            
        }
        opinion = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Diffusion.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(opinion.getPositiveRatio());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DiffusionUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_SOCIAL;
    }

    public int getPosition() {
        return 100;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "DiffusionUI.shortDescription");
    }
}
