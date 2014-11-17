package org.gephi.ui.generator.plugin;

import javax.swing.JCheckBox;
import org.gephi.lib.validation.BetweenZeroAndOneValidator;
import org.gephi.lib.validation.IntegerIntervalValidator;
import org.gephi.lib.validation.PositiveNumberValidator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

/**
 *
 * @author Alexandru Topirceanu
 */
public class ScaleFreeGraphPanel extends javax.swing.JPanel {

    private static final int minNodeDelay = 1;
    private static final int maxNodeDelay = 500;
    private static final int minEdgeDelay = 1;
    private static final int maxEdgeDelay = 100;
    
    /**
     * Creates new form RandomGraphPanel
     */
    public ScaleFreeGraphPanel() {
        initComponents();
    }

    public static ValidationPanel createValidationPanel(ScaleFreeGraphPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            innerPanel = new ScaleFreeGraphPanel();
        }
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        // graph parameters
        group.add(innerPanel.cellField, Validators.REQUIRE_NON_EMPTY_STRING, new PositiveNumberValidator());       
        group.add(innerPanel.seedSizeField, Validators.REQUIRE_NON_EMPTY_STRING, new PositiveNumberValidator());       
        group.add(innerPanel.seedWiringField, Validators.REQUIRE_NON_EMPTY_STRING, new BetweenZeroAndOneValidator());       
        group.add(innerPanel.fractalSizeField, Validators.REQUIRE_NON_EMPTY_STRING, new IntegerIntervalValidator(0, 1000));       
        // animation parameters
        group.add(innerPanel.tAnimateNode, Validators.REQUIRE_NON_EMPTY_STRING, new IntegerIntervalValidator(minNodeDelay, maxNodeDelay));
        group.add(innerPanel.tAnimateEdge, Validators.REQUIRE_NON_EMPTY_STRING, new IntegerIntervalValidator(minEdgeDelay, maxEdgeDelay));

        return validationPanel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        weightsGroup = new javax.swing.ButtonGroup();
        nodeLabel = new javax.swing.JLabel();
        cellField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        animateCheckBox = new javax.swing.JCheckBox();
        labelResolution = new org.jdesktop.swingx.JXLabel();
        tAnimateNode = new javax.swing.JTextField();
        labelResolution1 = new org.jdesktop.swingx.JXLabel();
        tAnimateEdge = new javax.swing.JTextField();
        labelResolution2 = new org.jdesktop.swingx.JXLabel();
        labelResolution3 = new org.jdesktop.swingx.JXLabel();
        radioNoWeights = new javax.swing.JRadioButton();
        radioNormalWeights = new javax.swing.JRadioButton();
        radioPowerWeights = new javax.swing.JRadioButton();
        nodeLabel1 = new javax.swing.JLabel();
        seedSizeField = new javax.swing.JTextField();
        nodeLabel2 = new javax.swing.JLabel();
        seedWiringField = new javax.swing.JTextField();
        checkDegree = new javax.swing.JCheckBox();
        checkBetweenness = new javax.swing.JCheckBox();
        nodeLabel3 = new javax.swing.JLabel();
        checkCloseness = new javax.swing.JCheckBox();
        checkCentrality = new javax.swing.JCheckBox();
        checkClustering = new javax.swing.JCheckBox();
        nodeLabel4 = new javax.swing.JLabel();
        fractalSizeField = new javax.swing.JTextField();

        nodeLabel.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.nodeLabel.text_2")); // NOI18N

        cellField.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.cellField.text_1")); // NOI18N

        jSeparator1.setForeground(new java.awt.Color(0, 0, 204));

        animateCheckBox.setSelected(true);
        animateCheckBox.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.animateCheckBox.text")); // NOI18N
        animateCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                animateCheckBoxStateChanged(evt);
            }
        });

        labelResolution.setForeground(new java.awt.Color(102, 102, 102));
        labelResolution.setLineWrap(true);
        labelResolution.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.labelResolution.text")); // NOI18N
        labelResolution.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelResolution.setFont(labelResolution.getFont().deriveFont(labelResolution.getFont().getSize()-1f));
        labelResolution.setPreferredSize(new java.awt.Dimension(500, 12));

        tAnimateNode.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.tAnimateNode.text")); // NOI18N

        labelResolution1.setForeground(new java.awt.Color(102, 102, 102));
        labelResolution1.setLineWrap(true);
        labelResolution1.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.labelResolution1.text")); // NOI18N
        labelResolution1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelResolution1.setFont(labelResolution1.getFont().deriveFont(labelResolution1.getFont().getSize()-1f));
        labelResolution1.setPreferredSize(new java.awt.Dimension(500, 12));

        tAnimateEdge.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.tAnimateEdge.text")); // NOI18N

        labelResolution2.setForeground(new java.awt.Color(102, 102, 102));
        labelResolution2.setLineWrap(true);
        labelResolution2.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.labelResolution2.text")); // NOI18N
        labelResolution2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelResolution2.setFont(labelResolution2.getFont().deriveFont(labelResolution2.getFont().getSize()-1f));
        labelResolution2.setPreferredSize(new java.awt.Dimension(500, 12));

        labelResolution3.setForeground(new java.awt.Color(0, 0, 204));
        labelResolution3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        labelResolution3.setLineWrap(true);
        labelResolution3.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.labelResolution3.text")); // NOI18N
        labelResolution3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelResolution3.setFont(labelResolution3.getFont().deriveFont(labelResolution3.getFont().getSize()-1f));
        labelResolution3.setPreferredSize(new java.awt.Dimension(500, 12));

        weightsGroup.add(radioNoWeights);
        radioNoWeights.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.radioNoWeights.text")); // NOI18N

        weightsGroup.add(radioNormalWeights);
        radioNormalWeights.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.radioNormalWeights.text")); // NOI18N

        weightsGroup.add(radioPowerWeights);
        radioPowerWeights.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.radioPowerWeights.text")); // NOI18N

        nodeLabel1.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.nodeLabel1.text")); // NOI18N

        seedSizeField.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.seedSizeField.text")); // NOI18N

        nodeLabel2.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.nodeLabel2.text")); // NOI18N

        seedWiringField.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.seedWiringField.text")); // NOI18N

        checkDegree.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.checkDegree.text")); // NOI18N

        checkBetweenness.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.checkBetweenness.text")); // NOI18N

        nodeLabel3.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.nodeLabel3.text")); // NOI18N

        checkCloseness.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.checkCloseness.text")); // NOI18N

        checkCentrality.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.checkCentrality.text")); // NOI18N

        checkClustering.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.checkClustering.text")); // NOI18N

        nodeLabel4.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.nodeLabel4.text")); // NOI18N

        fractalSizeField.setText(org.openide.util.NbBundle.getMessage(ScaleFreeGraphPanel.class, "ScaleFreeGraphPanel.fractalSizeField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(labelResolution3, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(animateCheckBox)
                                    .addComponent(tAnimateNode, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tAnimateEdge, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelResolution, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelResolution1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelResolution2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkDegree)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkBetweenness)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkCentrality)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkCloseness)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkClustering))
                            .addComponent(radioPowerWeights)
                            .addComponent(nodeLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(radioNormalWeights)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(nodeLabel4))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(nodeLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cellField, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(radioNoWeights))
                                        .addGap(43, 43, 43)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(nodeLabel1)
                                            .addComponent(nodeLabel2))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(seedSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(seedWiringField, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fractalSizeField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cellField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodeLabel)
                    .addComponent(nodeLabel1)
                    .addComponent(seedSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(radioNoWeights))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nodeLabel2)
                            .addComponent(seedWiringField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioNormalWeights)
                    .addComponent(nodeLabel4)
                    .addComponent(fractalSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(radioPowerWeights)
                .addGap(18, 18, 18)
                .addComponent(nodeLabel3)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkDegree)
                    .addComponent(checkBetweenness)
                    .addComponent(checkCentrality)
                    .addComponent(checkCloseness)
                    .addComponent(checkClustering))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelResolution, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(animateCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tAnimateNode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelResolution1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tAnimateEdge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelResolution2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(labelResolution3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void animateCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_animateCheckBoxStateChanged
        tAnimateNode.setEnabled(((JCheckBox) evt.getSource()).isSelected());
        tAnimateEdge.setEnabled(((JCheckBox) evt.getSource()).isSelected());
    }//GEN-LAST:event_animateCheckBoxStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JCheckBox animateCheckBox;
    protected javax.swing.JTextField cellField;
    protected javax.swing.JCheckBox checkBetweenness;
    protected javax.swing.JCheckBox checkCentrality;
    protected javax.swing.JCheckBox checkCloseness;
    protected javax.swing.JCheckBox checkClustering;
    protected javax.swing.JCheckBox checkDegree;
    protected javax.swing.JTextField fractalSizeField;
    private javax.swing.JSeparator jSeparator1;
    private org.jdesktop.swingx.JXLabel labelResolution;
    private org.jdesktop.swingx.JXLabel labelResolution1;
    private org.jdesktop.swingx.JXLabel labelResolution2;
    private org.jdesktop.swingx.JXLabel labelResolution3;
    private javax.swing.JLabel nodeLabel;
    private javax.swing.JLabel nodeLabel1;
    private javax.swing.JLabel nodeLabel2;
    private javax.swing.JLabel nodeLabel3;
    private javax.swing.JLabel nodeLabel4;
    protected javax.swing.JRadioButton radioNoWeights;
    protected javax.swing.JRadioButton radioNormalWeights;
    protected javax.swing.JRadioButton radioPowerWeights;
    protected javax.swing.JTextField seedSizeField;
    protected javax.swing.JTextField seedWiringField;
    protected javax.swing.JTextField tAnimateEdge;
    protected javax.swing.JTextField tAnimateNode;
    protected javax.swing.ButtonGroup weightsGroup;
    // End of variables declaration//GEN-END:variables
}