/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author walter
 */
public class RunKubeCtl {

    public int ret;
    public final StringBuilder in = new StringBuilder();
    public final StringBuilder err = new StringBuilder();

    public static RunKubeCtl apply(String yaml) throws Exception {
        ProcessBuilder pb;
        if (null == Global.getK8sConfig()) {
            pb = new ProcessBuilder(Global.kubeCtlPrg(), "apply", "-f", "-");
        } else {
            pb = new ProcessBuilder(Global.kubeCtlPrg(), "--kubeconfig=" + Global.getK8sConfig(), "--context=" + Global.getK8sContext(), "apply", "-f", "-");
        }
        RunKubeCtl rkc = new RunKubeCtl();
        rkc.run(pb, yaml, "kubectl apply");
        return rkc;
    }

    private void run(ProcessBuilder pb, String yaml, String action) throws Exception {
        final Process p = pb.start();
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
        if (null != yaml) {
            p.getOutputStream().write(yaml.getBytes(StandardCharsets.UTF_8));
            p.getOutputStream().write('\n');
        }
        p.getOutputStream().close();
        t1.join();
        t2.join();
        ret = p.waitFor();
    }

    public static RunKubeCtl delete(Metadata selected) throws Exception {
        ProcessBuilder pb;
        if (!selected.getNamespace().isEmpty()) {
            if (null == Global.getK8sConfig()) {
                pb = new ProcessBuilder(Global.kubeCtlPrg(), "delete", selected.getKind(), "-n", selected.getNamespace(), selected.getName());
            } else {
                pb = new ProcessBuilder(Global.kubeCtlPrg(), "--kubeconfig=" + Global.getK8sConfig(), "--context=" + Global.getK8sContext(), "delete", selected.getKind(), "-n", selected.getNamespace(), selected.getName());
            }
        } else {
            if (null == Global.getK8sConfig()) {
                pb = new ProcessBuilder(Global.kubeCtlPrg(), "delete", selected.getKind(), selected.getName());
            } else {
                pb = new ProcessBuilder(Global.kubeCtlPrg(), "--kubeconfig=" + Global.getK8sConfig(), "--context=" + Global.getK8sContext(), "delete", selected.getKind(), selected.getName());
            }
        }
        RunKubeCtl rkc = new RunKubeCtl();
        rkc.run(pb, null, "kubectl delete");
        return rkc;       
    }
}
