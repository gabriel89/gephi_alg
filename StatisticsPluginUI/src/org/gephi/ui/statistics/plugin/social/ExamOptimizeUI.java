package org.gephi.ui.statistics.plugin.social;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.social.ExamOptimize;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsUI.class)
public class ExamOptimizeUI implements StatisticsUI {

    //private final StatSettings settings = new StatSettings();
    private ExamOptimizePanel panel;
    private ExamOptimize exam;

    public JPanel getSettingsPanel() {
        if (panel == null) {
            panel = new ExamOptimizePanel();
        }
        return ExamOptimizePanel.createValidationPanel(panel);
    }

    public void setup(Statistics statistics) {
        this.exam = (ExamOptimize) statistics;
        if (panel != null) {
            panel.widthField.setText(exam.getWidth() + "");
            panel.lengthField.setText(exam.getHeight() + "");
            panel.iterationField.setText(exam.getK() + "");

            panel.netSize = exam.getN();            
            panel.setLinks(exam.getNeighborLinks());
        }
    }

    public void unsetup() {
        if (panel != null) {
            exam.setWidth(Integer.parseInt(panel.widthField.getText()));
            exam.setHeight(Integer.parseInt(panel.lengthField.getText()));
            exam.setK(Integer.parseInt(panel.iterationField.getText()));
            
            exam.setNeighborLinks(panel.getLinks());            
        }
        exam = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ExamOptimize.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(exam.getEfficiency());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "ExamOptimizeUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_SOCIAL;
    }

    public int getPosition() {
        return 200;
    }

    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "ExamOptimizeUI.shortDescription");
    }
}
