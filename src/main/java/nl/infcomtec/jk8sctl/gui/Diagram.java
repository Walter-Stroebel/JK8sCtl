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
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import nl.infcomtec.jk8sctl.CollectorUpdate;
import nl.infcomtec.jk8sctl.Global;
import nl.infcomtec.jk8sctl.K8sRelation;
import nl.infcomtec.jk8sctl.Maps;
import nl.infcomtec.jk8sctl.Metadata;

/**
 *
 * @author walter
 */
public class Diagram extends javax.swing.JFrame implements CollectorUpdate {

    /**
     * Creates new form Menu
     */
    public Diagram() {
        initComponents();
        setAlwaysOnTop(Global.getConfig().getModBoolean("diagram.alwaysontop", true, true));
        if (!Global.getConfig().restoreWindowPositionAndSize("diagram.window", this)) {
            Global.getConfig().saveWindowPositionAndSize("diagram.window", this);
        }
        Maps.doUpdate(this);
    }

    private StringBuilder genDot() {
        final StringBuilder dot = new StringBuilder();
        dot.append("strict digraph {\nrankdir=\"LR\";\nnode [shape=plaintext]\n");
        TreeSet<Integer> uniq = new TreeSet<>();
        for (Metadata item : Maps.items.values()) {
            switch (item.getKind()) {
                case "namespace":
                    if (!chNamespaces.isSelected()) {
                        continue;
                    }
                    break;
                case "deployment":
                    if (!chDeployments.isSelected()) {
                        continue;
                    }
                    break;
                case "pod":
                    if (!chPods.isSelected()) {
                        continue;
                    }
                    break;
                case "service":
                    if (!chServices.isSelected()) {
                        continue;
                    }
                    break;
                case "endpoints":
                    if (!chEndpoints.isSelected()) {
                        continue;
                    }
                    break;
                case "node":
                    if (!chNodes.isSelected()) {
                        continue;
                    }
                    break;
            }
            dot.append(item.getDotNode());
            draw(item.getMapId(), uniq, dot);
        }
        dot.append("}\n");
        return dot;
    }

    @Override
    public boolean update() {
        if (!isShowing()) {
            return false;
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    viewPanel.invalidate();
                    viewPanel.repaint();
                } catch (Exception ex) {
                    Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return true;
    }

    private class ViewPanel extends JPanel {

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
                    StringBuilder dot = genDot();
                    draw(dot, _g, w, h);
                } catch (Exception any) {
                    Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, any);
                }
            }
        }

    }

    private void draw(Integer mapId, TreeSet<Integer> uniq, final StringBuilder dot) {
        if (!uniq.add(mapId)) {
            return;
        }
        Metadata item = Maps.items.get(mapId);
        for (Map.Entry<Integer, K8sRelation> m : item.getRelations().entrySet()) {
            switch (m.getValue().label) {
                case "deployment":
                    if (!chDeployments.isSelected()) {
                        continue;
                    }
                    break;
                case "pod":
                    if (!chPods.isSelected()) {
                        continue;
                    }
                    break;
                case "namespace":
                case "ns":
                case "":
                    if (!chNamespaces.isSelected()) {
                        continue;
                    }
                    break;
                case "service":
                    if (!chServices.isSelected()) {
                        continue;
                    }
                    break;
                case "node":
                    if (!chNodes.isSelected()) {
                        continue;
                    }
                    break;
                case "endpoints":
                    if (!chEndpoints.isSelected()) {
                        continue;
                    }
                    break;
            }
            Metadata link = Maps.items.get(m.getValue().other);
            if (m.getValue().isTo) {
                dot.append(item.getDotNodeName()).append(" -> ").append(link.getDotNodeName()).append(m.getValue().toString());
            } else {
                dot.append(link.getDotNodeName()).append(" -> ").append(item.getDotNodeName()).append(m.getValue().toString());
            }
            draw(m.getKey(), uniq, dot);
        }
    }

    private void draw(final StringBuilder dot, Graphics _g, int w, int h) throws Exception {
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
                        Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                }
                try {
                    p.getInputStream().close();
                    baos.close();
                } catch (IOException ex) {
                    Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                }
                try {
                    p.getOutputStream().close();
                } catch (IOException ex) {
                    Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewPanel = new ViewPanel();
        jToolBar1 = new javax.swing.JToolBar();
        chNamespaces = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        chDeployments = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        chPods = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        chNodes = new javax.swing.JCheckBox();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        chServices = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        chEndpoints = new javax.swing.JCheckBox();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        butExportDOT = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cluster diagram");
        setAlwaysOnTop(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 763, Short.MAX_VALUE)
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 264, Short.MAX_VALUE)
        );

        getContentPane().add(viewPanel, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        chNamespaces.setSelected(true);
        chNamespaces.setText("Namespaces");
        chNamespaces.setFocusable(false);
        chNamespaces.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(chNamespaces);
        jToolBar1.add(jSeparator1);

        chDeployments.setSelected(true);
        chDeployments.setText("Deployments");
        chDeployments.setFocusable(false);
        chDeployments.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(chDeployments);
        jToolBar1.add(jSeparator2);

        chPods.setSelected(true);
        chPods.setText("Pods");
        chPods.setFocusable(false);
        chPods.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(chPods);
        jToolBar1.add(jSeparator3);

        chNodes.setSelected(true);
        chNodes.setText("Nodes");
        chNodes.setFocusable(false);
        chNodes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(chNodes);
        jToolBar1.add(jSeparator4);

        chServices.setText("Services");
        chServices.setFocusable(false);
        chServices.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(chServices);
        jToolBar1.add(jSeparator5);

        chEndpoints.setText("Endpoints");
        chEndpoints.setFocusable(false);
        chEndpoints.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(chEndpoints);
        jToolBar1.add(jSeparator6);

        butExportDOT.setText("Export DOT");
        butExportDOT.setFocusable(false);
        butExportDOT.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butExportDOT.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butExportDOT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butExportDOTActionPerformed(evt);
            }
        });
        jToolBar1.add(butExportDOT);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butExportDOTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butExportDOTActionPerformed
        JFileChooser jfc = new JFileChooser(Global.homeDir);
        jfc.setDialogTitle("Export SVG...");
        jfc.setFileFilter(new FileNameExtensionFilter("GraphViz files", "dot", "DOT"));
        int ans = jfc.showSaveDialog(this);
        if (ans == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(jfc.getSelectedFile())) {
                pw.print(genDot());
            } catch (Exception ex) {
                Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_butExportDOTActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        Global.getConfig().saveWindowPositionAndSize("diagram.window", this);
    }//GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        Global.getConfig().saveWindowPositionAndSize("diagram.window", this);
    }//GEN-LAST:event_formComponentResized

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
            java.util.logging.Logger.getLogger(Diagram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Diagram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Diagram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Diagram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Diagram().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butExportDOT;
    private javax.swing.JCheckBox chDeployments;
    private javax.swing.JCheckBox chEndpoints;
    private javax.swing.JCheckBox chNamespaces;
    private javax.swing.JCheckBox chNodes;
    private javax.swing.JCheckBox chPods;
    private javax.swing.JCheckBox chServices;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel viewPanel;
    // End of variables declaration//GEN-END:variables
}
