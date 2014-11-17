package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.CellularGraph;
import org.gephi.io.generator.plugin.CellularGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = CellularGraphUI.class)
public class CellularGraphUIImpl implements CellularGraphUI {

    private CellularGraphPanel panel;
    private CellularGraph cellularGraph;

    public CellularGraphUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new CellularGraphPanel();
        }
        return CellularGraphPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.cellularGraph = (CellularGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new CellularGraphPanel();
        }
        panel.cellField.setText(String.valueOf(cellularGraph.getNumberOfCells()));
        panel.intraEdgeField.setText(String.valueOf(cellularGraph.getIntraWiringProbability()));
        panel.interEdgeField.setText(String.valueOf(cellularGraph.getInterWiringProbability()));
        panel.cellSizeField.setText(String.valueOf(cellularGraph.getAvgCellSize()));
    }

    public void unsetup() {
        //Set params
        cellularGraph.setNumberOfCells(Integer.parseInt(panel.cellField.getText()));
        cellularGraph.setIntraWiringProbability(Double.parseDouble(panel.intraEdgeField.getText()));
        cellularGraph.setInterWiringProbability(Double.parseDouble(panel.interEdgeField.getText()));
        cellularGraph.setAvgCellSize(Integer.parseInt(panel.cellSizeField.getText()));
        panel = null;
    }
}
