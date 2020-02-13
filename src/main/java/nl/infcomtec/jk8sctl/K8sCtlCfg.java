/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author walter
 */
public class K8sCtlCfg {

    private static final String BIN_KUBECTL = "/bin/kubectl";
    private static final String SBIN_KUBECTL = "/sbin/kubectl";
    private static final String USR_BIN_KUBECTL = "/usr/bin/kubectl";
    private static final String USR_LOCAL_BIN_KUBECTL = "/usr/local/bin/kubectl";
    private static final String USR_LOCAL_SBIN_KUBECTL = "/usr/local/sbin/kubectl";
    private static final String USR_SBIN_KUBECTL = "/usr/sbin/kubectl";

    public static K8sCtlCfg defaults() {
        K8sCtlCfg ret = new K8sCtlCfg();
        String alt1 = System.getProperty("user.home") + File.separator + "bin" + File.separator + "kubectl";
        if (new File(SBIN_KUBECTL).exists()) {
            ret.kubeCtlProgam = SBIN_KUBECTL;
        } else if (new File(BIN_KUBECTL).exists()) {
            ret.kubeCtlProgam = BIN_KUBECTL;
        } else if (new File(USR_SBIN_KUBECTL).exists()) {
            ret.kubeCtlProgam = USR_SBIN_KUBECTL;
        } else if (new File(USR_BIN_KUBECTL).exists()) {
            ret.kubeCtlProgam = USR_LOCAL_BIN_KUBECTL;
        } else if (new File(USR_LOCAL_SBIN_KUBECTL).exists()) {
            ret.kubeCtlProgam = USR_LOCAL_SBIN_KUBECTL;
        } else if (new File(USR_LOCAL_BIN_KUBECTL).exists()) {
            ret.kubeCtlProgam = USR_LOCAL_BIN_KUBECTL;
        } else if (new File(alt1).exists()) {
            ret.kubeCtlProgam = alt1;
        } else {
            String[] tried = new String[]{SBIN_KUBECTL, BIN_KUBECTL, USR_SBIN_KUBECTL, USR_BIN_KUBECTL, USR_LOCAL_SBIN_KUBECTL, USR_LOCAL_BIN_KUBECTL, alt1};
            Global.warn("KubeCtl program not found, tried: %s", Arrays.toString(tried));
        }
        ret.yamlDir = System.getProperty("user.home");
        {
            File k = new File(Global.homeDir, ".kube");
            if (k.exists()) {
                k = new File(k, "config");
                if (k.exists()) {
                    ret.k8sConfig = k.getAbsolutePath();
                }
            }
        }
        return ret;
    }

    public static K8sCtlCfg loadConfig() throws IOException {
        Global.workDir.mkdirs();
        File f = new File(Global.workDir, "config.json");
        if (!f.exists()) {
            Global.warn("Using default configuration, '%s' not found", f.getCanonicalPath());
            return defaults();
        }
        try (FileReader reader = new FileReader(f)) {
            return Global.gson.fromJson(reader, K8sCtlCfg.class);
        }
    }
    /**
     * Config file for connections to Kubernetes. Normally ~/.kube/config.
     */
    public String k8sConfig;
    /**
     * kubectl program to use
     */
    public String kubeCtlProgam;
    /**
     * Default storage directory for Yaml files.
     */
    public String yamlDir;

    public void saveConfig() throws IOException {
        Global.workDir.mkdirs();
        try (FileWriter writer = new FileWriter(new File(Global.workDir, "config.json"))) {
            Global.gson.toJson(this, writer);
            writer.append('\n');
        }
    }
}
