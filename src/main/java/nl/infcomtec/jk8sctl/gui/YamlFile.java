/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl.gui;

import java.awt.BorderLayout;
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

    public final File src;
    public JTextArea jta;

    public YamlFile(File src) {
        super();
        this.src = src;
        setLayout(new BorderLayout());
        //JScrollPane sPane;
        try (StringWriter sw = new StringWriter()) {
            Yaml yaml = new Yaml();
            try (FileReader reader = new FileReader(src)) {
                Iterable<Object> loadAll = yaml.loadAll(reader);
                yaml.dumpAll(loadAll.iterator(), sw);
            }
            jta = new JTextArea(sw.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.toString());
            jta = new JTextArea("Error: " + ex.toString());
        }
        JScrollPane sPane = new JScrollPane(jta);
        add(sPane, BorderLayout.CENTER);
    }
}
