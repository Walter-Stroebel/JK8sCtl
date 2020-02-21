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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import nl.infcomtec.jk8sctl.K8sRelation;
import nl.infcomtec.jk8sctl.Maps;
import nl.infcomtec.jk8sctl.Metadata;

/**
 *
 * @author walter
 */
public class DotViewPanel extends JPanel {

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
