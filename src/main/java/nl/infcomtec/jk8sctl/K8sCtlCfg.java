/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.infcomtec.jk8salert.AlertSinkConfig;

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

    private static File configFile;

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

    public static File getConfigFile() {
        if (null == configFile) {
            Global.workDir.mkdirs(); // just in case we never ran
            return configFile = new File(Global.workDir, "config.json");
        }
        return configFile;
    }

    public static K8sCtlCfg loadConfig(String fromFile) throws IOException {
        configFile = new File(fromFile);
        return loadConfig();
    }

    public static K8sCtlCfg loadConfig() throws IOException {
        File f = getConfigFile();
        if (!f.exists()) {
            Global.warn("Using default configuration, '%s' not found", f.getCanonicalPath());
            return defaults();
        }
        try (FileReader reader = new FileReader(f)) {
            return Global.gson.fromJson(reader, K8sCtlCfg.class);
        }
    }
    /**
     * Configuration for Alerters.
     */
    public AlertSinkConfig[] alerters;
    /**
     * Config file for connections to Kubernetes. Normally ~/.kube/config.
     */
    public String k8sConfig;
    /**
     * Context to use inside the Kubernetes configuration.
     */
    public String k8sContext;
    /**
     * kubectl program to use
     */
    public String kubeCtlProgam;
    /**
     * Default storage directory for YAML files.
     */
    public String yamlDir;
    /**
     * Generic NVP storage for modules
     */
    public Map<String, Object> modConfig;

    public boolean restoreWindowPositionAndSize(String moduleKeyName, Frame comp) {
        if (null == modConfig) {
            return false;
        }
        try {
            Rectangle bounds = new Rectangle(
                    ((Double) modConfig.get(moduleKeyName + ".x")).intValue(),
                    ((Double) modConfig.get(moduleKeyName + ".y")).intValue(),
                    ((Double) modConfig.get(moduleKeyName + ".w")).intValue(),
                    ((Double) modConfig.get(moduleKeyName + ".h")).intValue());
            comp.setBounds(bounds);
        } catch (Exception any) {
            return false;
        }
        return true;
    }

    public void saveWindowPositionAndSize(String moduleKeyName, Frame comp) {
        if (null == modConfig) {
            modConfig = new TreeMap<>();
        }
        Rectangle bounds = comp.getBounds();
        modConfig.put(moduleKeyName + ".x", bounds.x);
        modConfig.put(moduleKeyName + ".y", bounds.y);
        modConfig.put(moduleKeyName + ".w", bounds.width);
        modConfig.put(moduleKeyName + ".h", bounds.height);
        try {
            saveConfig();
        } catch (IOException ex) {
            Logger.getLogger(K8sCtlCfg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getModString(String moduleKeyName, String defValue, boolean autoSave) {
        if (null == modConfig) {
            modConfig = new TreeMap<>();
        }
        Object obj = modConfig.get(moduleKeyName);
        if (null == obj) {
            modConfig.put(moduleKeyName, defValue);
            if (autoSave) {
                try {
                    saveConfig();
                } catch (IOException ex) {
                    Logger.getLogger(K8sCtlCfg.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return defValue;
        }
        if (!(obj instanceof String)) {
            Global.warn("Configuration error: module key name %s returns a %s but a String was expected", moduleKeyName, obj.getClass().getName());
            return defValue;
        }
        return (String) obj;
    }

    public boolean getModBoolean(String moduleKeyName, boolean defValue, boolean autoSave) {
        if (null == modConfig) {
            modConfig = new TreeMap<>();
        }
        Object obj = modConfig.get(moduleKeyName);
        if (null == obj) {
            modConfig.put(moduleKeyName, defValue);
            if (autoSave) {
                try {
                    saveConfig();
                } catch (IOException ex) {
                    Logger.getLogger(K8sCtlCfg.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return defValue;
        }
        if (!(obj instanceof Boolean)) {
            Global.warn("Configuration error: module key name %s returns a %s but a Boolean was expected", moduleKeyName, obj.getClass().getName());
            return defValue;
        }
        return (Boolean) obj;
    }

    /*
     * @throws IOException 
     */
    public void saveConfig() throws IOException {
        Global.workDir.mkdirs();
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            Global.gson.toJson(this, writer);
            writer.append('\n');
        }
    }
}
