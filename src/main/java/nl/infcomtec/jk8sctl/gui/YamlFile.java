/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl.gui;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author walter
 */
public class YamlFile extends JPanel {

    public File src;
    public final JTextArea jta;

    public YamlFile(File src) {
        super();
        this.src = src;
        setLayout(new BorderLayout());
        StringBuilder txt = new StringBuilder();
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(src))) {
                for (String s = reader.readLine(); null != s; s = reader.readLine()) {
                    txt.append(s).append("\n");
                }
            }
            try (StringWriter sw = new StringWriter()) {
                // test valid YAML
                Yaml yaml = new Yaml();
                Iterable<Object> loadAll = yaml.loadAll(txt.toString());
                yaml.dumpAll(loadAll.iterator(), sw);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.toString());
            txt.append("Error: ").append(ex.toString()).append("\n");
        }
        jta = new JTextArea(txt.toString());
        JScrollPane sPane = new JScrollPane(jta);
        add(sPane, BorderLayout.CENTER);
    }

    public YamlFile(String yText) {
        StringBuilder txt = new StringBuilder(yText);
        try {
            this.src = File.createTempFile("k8s", ".yml");
            this.src.deleteOnExit();
            setLayout(new BorderLayout());
            txt.append('\n');
            try (StringWriter sw = new StringWriter()) {
                // test valid YAML
                Yaml yaml = new Yaml();
                Iterable<Object> loadAll = yaml.loadAll(txt.toString());
                yaml.dumpAll(loadAll.iterator(), sw);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.toString());
            txt.append("Error: ").append(ex.toString()).append("\n");
        }
        jta = new JTextArea(txt.toString());
        JScrollPane sPane = new JScrollPane(jta);
        add(sPane, BorderLayout.CENTER);
    }

    public void backup() {
        int dot = src.getName().lastIndexOf('.');
        File bak = new File(src.getParentFile(), src.getName().substring(0, dot) + ".bak");
        if (bak.exists()) {
            bak.delete();
        }
        File ren = new File(src.getAbsolutePath());
        ren.renameTo(bak);
    }
}
