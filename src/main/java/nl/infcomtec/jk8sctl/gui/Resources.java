/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl.gui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import nl.infcomtec.jk8sctl.CollectorUpdate;
import nl.infcomtec.jk8sctl.Global;
import nl.infcomtec.jk8sctl.K8sResources;
import nl.infcomtec.jk8sctl.Maps;

/**
 *
 * @author walter
 */
public class Resources extends javax.swing.JFrame implements CollectorUpdate {

    /**
     * Creates new form Resources
     */
    public Resources() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Kubernetes Resources");
        initComponents();
        setAlwaysOnTop(Global.getConfig().getModBoolean("resources.alwaysontop", true, true));
        if (!Global.getConfig().restoreWindowPositionAndSize("resources.window", this)) {
            Global.getConfig().saveWindowPositionAndSize("resources.window", this);
        }
        Maps.doUpdate(this);
    }

    @Override
    public boolean update() {
        if (!isShowing()) {
            return false;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                getContentPane().removeAll();
                initComponents();
            }
        });
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Global.getConfig().saveWindowPositionAndSize("resources.window", Resources.this);
            }

            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                Global.getConfig().saveWindowPositionAndSize("resources.window", Resources.this);
            }
        });
        getContentPane().setLayout(new GridLayout(Maps.nodes.size() + 1, 5, 5, 5));
        getContentPane().add(new JLabel("Node"));
        getContentPane().add(new JLabel("CPU"));
        getContentPane().add(new JLabel("Memory"));
        getContentPane().add(new JLabel("Storage"));
        getContentPane().add(new JLabel("Pods"));

        for (Map.Entry<String, K8sResources> e : Maps.nodes.entrySet()) {
            getContentPane().add(new JLabel(e.getKey()));
            {
                JProgressBar pg = new JProgressBar();
                pg.setMinimum(0);
                pg.setMaximum(255);
                int v = e.getValue().cpu();
                pg.setValue(v);
                getContentPane().add(pg);
            }
            {
                JProgressBar pg = new JProgressBar();
                pg.setMinimum(0);
                pg.setMaximum(255);
                int v = e.getValue().mem();
                pg.setValue(v);
                getContentPane().add(pg);
            }
            {
                JProgressBar pg = new JProgressBar();
                pg.setMinimum(0);
                pg.setMaximum(255);
                int v = e.getValue().dsk();
                pg.setValue(v);
                getContentPane().add(pg);
            }
            {
                JProgressBar pg = new JProgressBar();
                pg.setMinimum(0);
                pg.setMaximum(255);
                int v = e.getValue().pod();
                pg.setValue(v);
                getContentPane().add(pg);
            }
        }
        pack();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Resources.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Resources.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Resources.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Resources.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Resources().setVisible(true);
            }
        });
    }
}
