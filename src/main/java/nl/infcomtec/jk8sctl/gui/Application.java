/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl.gui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import nl.infcomtec.jk8sctl.K8sApplication;
import nl.infcomtec.jk8sctl.K8sDeployment;
import nl.infcomtec.jk8sctl.K8sPod;
import nl.infcomtec.jk8sctl.K8sReplicationController;
import nl.infcomtec.jk8sctl.Maps;
import nl.infcomtec.jk8sctl.Metadata;
import nl.infcomtec.jk8sctl.RunKubeCtl;

/**
 *
 * @author walter
 */
public class Application extends javax.swing.JFrame {

    /**
     * Creates new form Application
     */
    public Application() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        butClone = new javax.swing.JButton();
        cbCloneable = new javax.swing.JComboBox<>();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        butClear = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        butApply = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        butDelete = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        butYaml = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        jLabel9 = new javax.swing.JLabel();
        typePod = new javax.swing.JRadioButton();
        typeDeploy = new javax.swing.JRadioButton();
        typeReplCtrl = new javax.swing.JRadioButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        chNamespace = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        butAddDetail = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cbNamespace = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        tfAppName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tfDockImg = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        tfDockArgs = new javax.swing.JTextField();
        lbDockCmd = new javax.swing.JLabel();
        tfDockCmd = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        butClone.setText("Clone ->");
        butClone.setFocusable(false);
        butClone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butClone.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butClone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCloneActionPerformed(evt);
            }
        });
        jToolBar1.add(butClone);

        cbCloneable.setModel(new javax.swing.DefaultComboBoxModel<>(CloneableObjects.asArray()));
        jToolBar1.add(cbCloneable);
        jToolBar1.add(jSeparator6);

        butClear.setText("Clear fields");
        butClear.setFocusable(false);
        butClear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butClear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butClearActionPerformed(evt);
            }
        });
        jToolBar1.add(butClear);
        jToolBar1.add(jSeparator3);

        butApply.setText("Apply");
        butApply.setFocusable(false);
        butApply.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butApply.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butApplyActionPerformed(evt);
            }
        });
        jToolBar1.add(butApply);
        jToolBar1.add(jSeparator4);

        butDelete.setText("Delete");
        butDelete.setFocusable(false);
        butDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDeleteActionPerformed(evt);
            }
        });
        jToolBar1.add(butDelete);
        jToolBar1.add(jSeparator5);

        butYaml.setText("Show YAML");
        butYaml.setToolTipText("Preview as Yaml");
        butYaml.setFocusable(false);
        butYaml.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butYaml.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butYaml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butYamlActionPerformed(evt);
            }
        });
        jToolBar1.add(butYaml);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jLabel9.setText("Generator settings -> ");
        jToolBar2.add(jLabel9);

        buttonGroup1.add(typePod);
        typePod.setText("Pod");
        typePod.setFocusable(false);
        typePod.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                typePodStateChanged(evt);
            }
        });
        jToolBar2.add(typePod);

        buttonGroup1.add(typeDeploy);
        typeDeploy.setSelected(true);
        typeDeploy.setText("Deployment");
        typeDeploy.setFocusable(false);
        typeDeploy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        typeDeploy.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                typeDeployStateChanged(evt);
            }
        });
        jToolBar2.add(typeDeploy);

        buttonGroup1.add(typeReplCtrl);
        typeReplCtrl.setText("ReplCtrl");
        typeReplCtrl.setFocusable(false);
        typeReplCtrl.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(typeReplCtrl);
        jToolBar2.add(jSeparator7);

        chNamespace.setText("Generate namespace");
        chNamespace.setFocusable(false);
        chNamespace.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(chNamespace);
        jToolBar2.add(jSeparator2);

        butAddDetail.setText("Add detail");
        butAddDetail.setFocusable(false);
        butAddDetail.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butAddDetail.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butAddDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAddDetailActionPerformed(evt);
            }
        });
        jToolBar2.add(butAddDetail);

        jLabel1.setText("Namespace");

        cbNamespace.setEditable(true);
        cbNamespace.setModel(new NamespaceModel());
        cbNamespace.setToolTipText("Either a new namespace or pick an existing one");

        jLabel2.setText("Application name");

        tfAppName.setToolTipText("Should be a short but descriptive unique name");

        jLabel3.setText("Docker image");

        tfDockImg.setToolTipText("For instance \"ubuntu:latest\" or \"osrm/osrm-backend\"");

        jLabel10.setText("Docker args");
        jLabel10.setToolTipText("Arguments to pass to Docker");

        lbDockCmd.setText("Docker command");

        tfDockCmd.setToolTipText("Optionally, depending on the application, the command line");

        jTable1.setModel(new Details());
        jTable1.setRowSelectionAllowed(false);
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 780, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lbDockCmd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(14, 14, 14)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cbNamespace, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfAppName, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfDockImg, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfDockArgs, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfDockCmd, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 776, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 778, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel1))
                    .addComponent(cbNamespace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel2))
                    .addComponent(tfAppName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel3))
                    .addComponent(tfDockImg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel10))
                    .addComponent(tfDockArgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfDockCmd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(lbDockCmd)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void butCloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCloneActionPerformed
        butClearActionPerformed(evt);
        String app = (String) cbCloneable.getSelectedItem();
        if (null == app) {
            JOptionPane.showMessageDialog(this, "Nothing selected to clone");
            return;
        }
        TreeSet<Integer> get = Maps.apps.get(app);
        if (null == get) {
            JOptionPane.showMessageDialog(this, "Selected object disappeared");
            return;
        }
        for (int ik : get) {
            Metadata item = Maps.items.get(ik);
            if (item instanceof K8sDeployment) {
                K8sDeployment it = (K8sDeployment) item;
                application = new K8sApplication(it);
                typeDeploy.setSelected(true);
                break;
            } else if (item instanceof K8sReplicationController) {
                K8sReplicationController it = (K8sReplicationController) item;
                application = new K8sApplication(it);
                typeReplCtrl.setSelected(true);
                break;
            }
        }
        if (null == application) {
            for (int ik : get) {
                Metadata item = Maps.items.get(ik);
                if (item instanceof K8sPod) {
                    K8sPod it = (K8sPod) item;
                    application = new K8sApplication(it);
                    typePod.setSelected(true);
                    break;
                }
            }
        }
        if (null != application) {
            tfAppName.setText(application.appName);
            {
                StringBuilder sb = new StringBuilder();
                for (String s : application.args) {
                    sb.append(' ').append(s);
                }
                tfDockArgs.setText(sb.toString().trim());
            }
            {
                StringBuilder sb = new StringBuilder();
                for (String s : application.command) {
                    sb.append(' ').append(s);
                }
                tfDockCmd.setText(sb.toString().trim());
            }
            tfDockImg.setText(application.image);
            cbNamespace.setSelectedItem(application.namespace);
        }
        updateTable();
    }//GEN-LAST:event_butCloneActionPerformed

    private void updateTable() {
        ((Details) jTable1.getModel()).fireTableDataChanged();
    }

    private void butYamlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butYamlActionPerformed
        tfAppName.setText(dnsClean(tfAppName.getText()));
        cbNamespace.setSelectedItem(dnsClean((String) cbNamespace.getSelectedItem()));
        ShowText.showText(tfAppName.getText().trim().isEmpty() ? "Untitled" : tfAppName.getText().trim(), toYamlString());
    }//GEN-LAST:event_butYamlActionPerformed
    private K8sApplication application;

    private class Details extends AbstractTableModel {

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Detail";
                case 1:
                    return "Name";
                case 2:
                    return "Source";
                case 3:
                    return "Target";
            }
            return "?";
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public int getRowCount() {
            if (null == application) {
                return 0;
            }
            return application.details.size();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            K8sApplication.Detail d = application.details.get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    String s = (String) aValue;
                    if (null == s || s.isEmpty()) {
                        d.type = "Remove";
                    } else if (s.toUpperCase().startsWith("D")) {
                        d.type = "DirectoryOrCreate";
                    } else if (s.toUpperCase().startsWith("T")) {
                        d.type = "TCP";
                    } else if (s.toUpperCase().startsWith("U")) {
                        d.type = "UDP";
                    } else {
                        d.type = "Remove";
                    }
                }
                break;
                case 1:
                    d.name = (String) aValue;
                    break;
                case 2:
                    d.source = (String) aValue;
                    break;
                case 3:
                    d.target = (String) aValue;
                    break;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            K8sApplication.Detail d = application.details.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return d.type;
                case 1:
                    return d.name;
                case 2:
                    return d.source;
                case 3:
                    return d.target;
            }
            return "?";
        }

    }
    private void butClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butClearActionPerformed
        tfAppName.setText("");
        tfDockArgs.setText("");
        tfDockCmd.setText("");
        tfDockImg.setText("");
        cbCloneable.setSelectedItem("");
        cbNamespace.setSelectedItem("");
        application = null;
        updateTable();
    }//GEN-LAST:event_butClearActionPerformed

    private void typePodStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_typePodStateChanged

    }//GEN-LAST:event_typePodStateChanged

    private void typeDeployStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_typeDeployStateChanged

    }//GEN-LAST:event_typeDeployStateChanged

    private void butAddDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAddDetailActionPerformed
        if (null == application) {
            application = new K8sApplication();
        }
        K8sApplication.Detail m = new K8sApplication.Detail();
        m.name = "new";
        m.source = "";
        m.target = "";
        application.details.add(m);
        updateTable();
    }//GEN-LAST:event_butAddDetailActionPerformed

    private void butApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butApplyActionPerformed
        try {
            RunKubeCtl.apply(toYamlString());
        } catch (Exception ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_butApplyActionPerformed

    private void butDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleteActionPerformed
        ItemSelector.select(new SelectedItemAction() {
            @Override
            public String topLine() {
                return "Objects that can be deleted. Warning: can destroy your cluster!";
            }

            @Override
            public boolean withSelected(Metadata item) {
                KubeCtlAction.delete(item);
                return true;
            }
        });
    }//GEN-LAST:event_butDeleteActionPerformed

    private String dnsClean(String namespace) {
        StringBuilder clean = new StringBuilder();
        for (char c : namespace.toCharArray()) {
            if (clean.length() == 0) {
                if (Character.isLetter(c)) {
                    clean.append(c);
                }
            } else if (Character.isLetterOrDigit(c)) {
                clean.append(c);
            } else if (c == '-') {
                clean.append(c);
            }
        }
        return clean.toString();
    }

    private String toYamlString() {
        if (null == application) {
            application = new K8sApplication();
        } else {
            for (Iterator<K8sApplication.Detail> it = application.details.iterator(); it.hasNext();) {
                K8sApplication.Detail d = it.next();
                if (null == d.type || d.type.isEmpty()) {
                    it.remove();
                } else if (d.type.toUpperCase().startsWith("D")) {
                    d.type = "DirectoryOrCreate";
                } else if (d.type.toUpperCase().startsWith("T")) {
                    d.type = "TCP";
                } else if (d.type.toUpperCase().startsWith("U")) {
                    d.type = "UDP";
                } else {
                    it.remove();
                }
            }
        }
        application.appName = tfAppName.getText();
        application.args = new LinkedList<>();
        {
            StringTokenizer toker = new StringTokenizer(tfDockArgs.getText(), " \"", true);
            StringBuilder quoted = null;
            while (toker.hasMoreTokens()) {
                String tok = toker.nextToken();
                if (null != quoted) {
                    if (tok.equals("\"")) {
                        application.args.add(quoted.toString());
                        quoted = null;
                    } else {
                        quoted.append(tok);
                    }
                } else if (tok.equals("\"")) {
                    quoted = new StringBuilder();
                } else if (!tok.equals(" ")) {
                    application.args.add(tok);
                }
            }
        }
        application.command = new LinkedList<>();
        {
            StringTokenizer toker = new StringTokenizer(tfDockCmd.getText(), " \"");
            StringBuilder quoted = null;
            while (toker.hasMoreTokens()) {
                String tok = toker.nextToken();
                if (null != quoted) {
                    if (tok.equals("\"")) {
                        application.command.add(quoted.toString());
                        quoted = null;
                    } else {
                        quoted.append(tok);
                    }
                } else if (tok.equals("\"")) {
                    quoted = new StringBuilder();
                } else if (!tok.equals(" ")) {
                    application.command.add(tok);
                }
            }
        }
        application.image = tfDockImg.getText();
        application.namespace = (String) cbNamespace.getSelectedItem();
        updateTable();
        if (typePod.isSelected()) {
            return application.toYaml(K8sApplication.Templates.Pod, chNamespace.isSelected());
        } else if (typeDeploy.isSelected()) {
            return application.toYaml(K8sApplication.Templates.Deployment, chNamespace.isSelected());
        } else {
            return application.toYaml(K8sApplication.Templates.ReplicationController, chNamespace.isSelected());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Application.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Application.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Application.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Application.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Application().setVisible(true);
            }
        });
    }

    private class NamespaceModel extends DefaultComboBoxModel<String> {

        NamespaceModel() {
            super(Maps.getSpaces());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butAddDetail;
    private javax.swing.JButton butApply;
    private javax.swing.JButton butClear;
    private javax.swing.JButton butClone;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butYaml;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbCloneable;
    private javax.swing.JComboBox<String> cbNamespace;
    private javax.swing.JCheckBox chNamespace;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JTable jTable1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JLabel lbDockCmd;
    private javax.swing.JTextField tfAppName;
    private javax.swing.JTextField tfDockArgs;
    private javax.swing.JTextField tfDockCmd;
    private javax.swing.JTextField tfDockImg;
    private javax.swing.JRadioButton typeDeploy;
    private javax.swing.JRadioButton typePod;
    private javax.swing.JRadioButton typeReplCtrl;
    // End of variables declaration//GEN-END:variables
}
