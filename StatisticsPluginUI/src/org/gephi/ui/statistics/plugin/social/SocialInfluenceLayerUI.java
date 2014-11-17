package org.gephi.ui.statistics.plugin.social;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.social.SocialInfluenceLayer;
import org.gephi.statistics.plugin.social.SocialInfluenceLayer.Fitness;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsUI.class)
public class SocialInfluenceLayerUI implements StatisticsUI {

    //private final StatSettings settings = new StatSettings();
    private SocialInfluenceLayerPanel panel;
    private SocialInfluenceLayer socialInfluence;

    public JPanel getSettingsPanel() {
        if (panel == null) {
            panel = new SocialInfluenceLayerPanel();
        }
        return SocialInfluenceLayerPanel.createValidationPanel(panel);
    }

    public void setup(Statistics statistics) {
        this.socialInfluence = (SocialInfluenceLayer) statistics;
        if (panel != null) {
            List<Fitness> fitnesses = socialInfluence.getFitnesses();

            if (fitnesses == null) {
                panel.checkBtw.setSelected(true);
                return;
            }

            panel.checkDegree.setSelected(fitnesses.contains(Fitness.DEGREE));
            panel.checkBtw.setSelected(fitnesses.contains(Fitness.BETWEENNESS));
            panel.checkEigen.setSelected(fitnesses.contains(Fitness.EIGENVECTOR));
            panel.checkClose.setSelected(fitnesses.contains(Fitness.CLOSENESS));
            panel.checkClustering.setSelected(fitnesses.contains(Fitness.CLUSTERING));
        }
    }

    public void unsetup() {
        if (panel != null) {
            List<Fitness> fitnesses = new ArrayList<Fitness>();
            if (panel.checkDegree.isSelected()) {
                fitnesses.add(Fitness.DEGREE);
            }
            if (panel.checkBtw.isSelected()) {
                fitnesses.add(Fitness.BETWEENNESS);
            }
            if (panel.checkEigen.isSelected()) {
                fitnesses.add(Fitness.EIGENVECTOR);
            }
            if (panel.checkClose.isSelected()) {
                fitnesses.add(Fitness.CLOSENESS);
            }
            if (panel.checkClustering.isSelected()) {
                fitnesses.add(Fitness.CLUSTERING);
            }

            socialInfluence.setFitnesses(fitnesses);
        }
        socialInfluence = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return SocialInfluenceLayer.class;
    }

    public String getValue() {
        return "Done";
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "SocialInfluenceLayerUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_SOCIAL;
    }

    public int getPosition() {
        return 600;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "SocialInfluenceLayerUI.shortDescription");
    }
}
