/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;

/**
 *
 * @author walter
 */
public class RunKubeCtl {

    public static void apply(String yaml) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(Global.kubeCtlPrg(), "apply", "-f", "-");
        final Process p = pb.start();
        final StringBuilder in = new StringBuilder();
        final StringBuilder err = new StringBuilder();
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int c = p.getInputStream().read();
                        if (c < 0) {
                            break;
                        }
                        in.append((char) c);
                    } catch (IOException ex) {
                    }
                }
            }
        };
        t1.start();
        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int c = p.getErrorStream().read();
                        if (c < 0) {
                            break;
                        }
                        err.append((char) c);
                    } catch (IOException ex) {
                    }
                }
            }
        };
        t2.start();
        p.getOutputStream().write(yaml.getBytes(StandardCharsets.UTF_8));
        p.getOutputStream().write('\n');
        p.getOutputStream().close();
        t1.join();
        t2.join();
        int ret = p.waitFor();
        StringBuilder msg = new StringBuilder();
        msg.append(in.toString());
        if (err.length() > 0) {
            msg.append("\nErrors:\n");
            msg.append(err.toString());
        }
        msg.append("\nReturn code: ").append(ret);
        System.out.println(msg);
        JOptionPane.showMessageDialog(null, msg.toString(), "kubectl apply", JOptionPane.INFORMATION_MESSAGE);
    }
}
