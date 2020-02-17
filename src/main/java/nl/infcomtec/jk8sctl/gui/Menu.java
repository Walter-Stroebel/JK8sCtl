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
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import nl.infcomtec.jk8sctl.Global;
import nl.infcomtec.jk8sctl.K8sCtlCfg;
import nl.infcomtec.jk8sctl.K8sRelation;
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
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Maps.collect();
                } catch (Exception ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
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

        jToolBar1 = new javax.swing.JToolBar();
        butConnect = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        butCreate = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        butDelete = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        Debug = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        butDiagram = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Kubernetes Tools by InfComTec");

        jToolBar1.setRollover(true);

        butConnect.setText("Connect");
        butConnect.setFocusable(false);
        butConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butConnectActionPerformed(evt);
            }
        });
        jToolBar1.add(butConnect);
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

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butConnectActionPerformed
        JFileChooser jfc = new JFileChooser(new File(Global.homeDir, ".kube"));
        jfc.setApproveButtonText("Select");
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            K8sCtlCfg config = Global.getConfig();
            config.k8sConfig = jfc.getSelectedFile().getAbsolutePath();
            try {
                config.saveConfig();
            } catch (Exception ex) {
                Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_butConnectActionPerformed

    private void butCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCreateActionPerformed
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                final DeployService dialog = new DeployService(new javax.swing.JFrame(), false);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    @Override
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        dialog.dispose();
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
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
        if (!Maps.items.isEmpty()){
            ItemTree.main(new String[]{});
        }
    }//GEN-LAST:event_DebugActionPerformed

    private void butDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleteActionPerformed
        Armageddon.main(new String[]{});
    }//GEN-LAST:event_butDeleteActionPerformed

    private void butDiagramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDiagramActionPerformed
        Diagram.main(null);
    }//GEN-LAST:event_butDiagramActionPerformed

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
    private javax.swing.JButton butConnect;
    private javax.swing.JButton butCreate;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butDiagram;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}