/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import nl.infcomtec.jk8sctl.Global;
import nl.infcomtec.jk8sctl.K8sCondition;
import nl.infcomtec.jk8sctl.K8sCtlCfg;
import nl.infcomtec.jk8sctl.K8sRelation;
import nl.infcomtec.jk8sctl.K8sStatus;
import nl.infcomtec.jk8sctl.Maps;
import nl.infcomtec.jk8sctl.Metadata;

/**
 *
 * @author walter
 */
public class Menu extends javax.swing.JFrame {

    /**
     * Creates new form Menu
     */
    public Menu() {
        initComponents();
        pack();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Maps.collect();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            StatusTableModel tm = new StatusTableModel();
                            for (Metadata item : Maps.items.values()) {
                                K8sStatus status = item.getStatus();
                                if (!status.okay || chShowAllConditions.isSelected()) {
                                    for (K8sCondition c : status.details.values()) {
                                        String[] line = new String[5];
                                        line[0] = item.getName();
                                        line[1] = item.getKind();
                                        line[2] = c.type;
                                        line[3] = c.status.toString();
                                        line[4] = c.lastUpdateAge;
                                        tm.content.add(line);
                                    }
                                }
                            }
                            statusTable.setModel(tm);
                        }
                    });
                } catch (Exception ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            class StatusTableModel extends AbstractTableModel {

                @Override
                public String getColumnName(int column) {
                    switch (column) {
                        case 0:
                            return "Component";
                        case 1:
                            return "Kind";
                        case 2:
                            return "Condition";
                        case 3:
                            return "Status";
                        case 4:
                            return "Age";
                    }
                    return "?";
                }

                ArrayList<String[]> content = new ArrayList<>();

                @Override
                public int getColumnCount() {
                    return 5;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }

                @Override
                public int getRowCount() {
                    return content.size();
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return content.get(rowIndex)[columnIndex];
                }
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    private static class ViewPanel extends JPanel {

        @Override
        public void paint(Graphics _g) {
            int w = getWidth();
            int h = getHeight();
            if (Maps.items.isEmpty()) {
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = bi.createGraphics();
                g.setColor(Color.ORANGE);
                g.fillRect(0, 0, w, h);
                g.setColor(Color.BLACK);
                g.drawLine(0, 0, w - 1, h - 1);
                g.drawLine(0, h - 1, w - 1, 0);
                g.dispose();
                _g.drawImage(bi, 0, 0, null);
            } else {
                try {
                    final StringBuilder dot = new StringBuilder();
                    dot.append("strict digraph {\nrankdir=\"LR\";\nnode [shape=plaintext]\n");
                    TreeSet<Integer> uniq = new TreeSet<>();
                    for (Metadata item : Maps.items.values()) {
                        dot.append(item.getDotNode());
                        draw(item.getMapId(), uniq, dot);
                    }
                    dot.append("}\n");
                    draw(dot, _g, w, h);
                } catch (Exception any) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, any);
                }
            }
        }

        private void draw(Integer mapId, TreeSet<Integer> uniq, final StringBuilder dot) {
            if (!uniq.add(mapId)) {
                return;
            }
            Metadata item = Maps.items.get(mapId);
            for (Map.Entry<Integer, K8sRelation> m : item.getRelations().entrySet()) {
                Metadata link = Maps.items.get(m.getValue().other);
                if (m.getValue().isTo) {
                    dot.append(item.getDotNodeName()).append(" -> ").append(link.getDotNodeName()).append(m.getValue().toString());
                } else {
                    dot.append(link.getDotNodeName()).append(" -> ").append(item.getDotNodeName()).append(m.getValue().toString());
                }
                draw(m.getKey(), uniq, dot);
            }
        }

        private void draw(final StringBuilder dot, Graphics _g, int w, int h) throws InterruptedException, IOException {
            //System.out.println(dot);
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng");
            pb.redirectError(new File("/dev/null"));
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Process p = pb.start();
            Thread t1 = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            int c = p.getInputStream().read();
                            if (c >= 0) {
                                baos.write(c);
                            } else {
                                break;
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        }
                    }
                    try {
                        p.getInputStream().close();
                        baos.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            Thread t2 = new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < dot.length(); i++) {
                        try {
                            p.getOutputStream().write(dot.charAt(i));
                        } catch (IOException ex) {
                            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        }
                    }
                    try {
                        p.getOutputStream().close();
                    } catch (IOException ex) {
                        Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            t1.start();
            t2.start();
            t2.join();
            t1.join();
            p.waitFor();
            BufferedImage png = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
            _g.drawImage(png.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH), 0, 0, null);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jToolBar1 = new javax.swing.JToolBar();
        butSelCluster = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        butCreate = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        butDelete = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        Debug = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        butDiagram = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        butYamlEditor = new javax.swing.JButton();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        chShowAllConditions = new javax.swing.JCheckBox();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jToolBar2 = new javax.swing.JToolBar();
        butLoadConfig = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        butSaveConfig = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        butResetConfig = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        butEditConfig = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        statusTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Kubernetes Tools by InfComTec");

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        butSelCluster.setText("Select cluster");
        butSelCluster.setFocusable(false);
        butSelCluster.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSelCluster.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butSelCluster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSelClusterActionPerformed(evt);
            }
        });
        jToolBar1.add(butSelCluster);
        jToolBar1.add(jSeparator1);

        butCreate.setText("Create");
        butCreate.setFocusable(false);
        butCreate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butCreate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCreateActionPerformed(evt);
            }
        });
        jToolBar1.add(butCreate);
        jToolBar1.add(jSeparator2);

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
        jToolBar1.add(jSeparator3);

        Debug.setText("Debug");
        Debug.setFocusable(false);
        Debug.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Debug.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Debug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DebugActionPerformed(evt);
            }
        });
        jToolBar1.add(Debug);
        jToolBar1.add(jSeparator4);

        butDiagram.setText("Diagram");
        butDiagram.setFocusable(false);
        butDiagram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butDiagram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDiagramActionPerformed(evt);
            }
        });
        jToolBar1.add(butDiagram);
        jToolBar1.add(jSeparator5);

        butYamlEditor.setText("YAML editor");
        butYamlEditor.setFocusable(false);
        butYamlEditor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butYamlEditor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butYamlEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butYamlEditorActionPerformed(evt);
            }
        });
        jToolBar1.add(butYamlEditor);
        jToolBar1.add(jSeparator10);

        chShowAllConditions.setText("Show all conditions");
        chShowAllConditions.setFocusable(false);
        chShowAllConditions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(chShowAllConditions);
        jToolBar1.add(jSeparator6);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        butLoadConfig.setText("Load config");
        butLoadConfig.setFocusable(false);
        butLoadConfig.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butLoadConfig.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butLoadConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLoadConfigActionPerformed(evt);
            }
        });
        jToolBar2.add(butLoadConfig);
        jToolBar2.add(jSeparator9);

        butSaveConfig.setText("Save config");
        butSaveConfig.setFocusable(false);
        butSaveConfig.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSaveConfig.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butSaveConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSaveConfigActionPerformed(evt);
            }
        });
        jToolBar2.add(butSaveConfig);
        jToolBar2.add(jSeparator7);

        butResetConfig.setText("Reset config");
        butResetConfig.setFocusable(false);
        butResetConfig.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butResetConfig.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butResetConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butResetConfigActionPerformed(evt);
            }
        });
        jToolBar2.add(butResetConfig);
        jToolBar2.add(jSeparator8);

        butEditConfig.setText("Edit config");
        butEditConfig.setFocusable(false);
        butEditConfig.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butEditConfig.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butEditConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butEditConfigActionPerformed(evt);
            }
        });
        jToolBar2.add(butEditConfig);

        statusTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Component", "Kind", "Condition", "Status", "Age"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(statusTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 727, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCreateActionPerformed
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
    }//GEN-LAST:event_butCreateActionPerformed

    private void DebugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DebugActionPerformed
        if (!Maps.items.isEmpty()) {
            ItemTree.main(new String[]{});
        }
    }//GEN-LAST:event_DebugActionPerformed

    private void butDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleteActionPerformed
        Armageddon.main(new String[]{});
    }//GEN-LAST:event_butDeleteActionPerformed

    private void butDiagramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDiagramActionPerformed
        Diagram.main(null);
    }//GEN-LAST:event_butDiagramActionPerformed

    private void butYamlEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butYamlEditorActionPerformed
        YamlEditor.main(null);
    }//GEN-LAST:event_butYamlEditorActionPerformed

    private void butSaveConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSaveConfigActionPerformed
        try {
            Global.getConfig().saveConfig();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.toString());
            Logger.getLogger(YamlEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_butSaveConfigActionPerformed

    private void butResetConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butResetConfigActionPerformed
        try {
            K8sCtlCfg.defaults().saveConfig();
            Global.loadConfig();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.toString());
        }
    }//GEN-LAST:event_butResetConfigActionPerformed

    private void butEditConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butEditConfigActionPerformed
        EditText.showText(K8sCtlCfg.getConfigFile(), Global.gson.toJson(Global.getConfig()));
    }//GEN-LAST:event_butEditConfigActionPerformed

    private void butLoadConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butLoadConfigActionPerformed
        Global.loadConfig();
    }//GEN-LAST:event_butLoadConfigActionPerformed

    private void butSelClusterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSelClusterActionPerformed
        SelectCluster.main(null);
    }//GEN-LAST:event_butSelClusterActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            Maps.collect();
        } catch (Exception ex) {
            // we tried
        }
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
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Menu().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Debug;
    private javax.swing.JButton butCreate;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butDiagram;
    private javax.swing.JButton butEditConfig;
    private javax.swing.JButton butLoadConfig;
    private javax.swing.JButton butResetConfig;
    private javax.swing.JButton butSaveConfig;
    private javax.swing.JButton butSelCluster;
    private javax.swing.JButton butYamlEditor;
    private javax.swing.JCheckBox chShowAllConditions;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JTable statusTable;
    // End of variables declaration//GEN-END:variables
}
