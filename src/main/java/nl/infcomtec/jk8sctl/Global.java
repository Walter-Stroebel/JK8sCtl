/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Holds all global data and configuration.
 *
 * @author walter
 */
public class Global {

    public static final long MiB = 1024 * 1024;
    public static final double MiBf = 1024.0 * 1024.0;
    public static final long KiB = 1024;
    public static final double KiBf = 1024.0;
    public static final long GiB = 1024 * 1024 * 1024;
    public static final double GiBf = 1024.0 * 1024.0 * 1024.0;
    public static final long TiB = 1024 * 1024 * 1024 * 1024;
    public static final double TGiBf = 1024.0 * 1024.0 * 1024.0 * 1024.0;

    /**
     * Global Gson engine with readability option set.
     */
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    /**
     * Internal copy of the configuration.
     */
    private static K8sCtlCfg config;
    /**
     * Best stab at the user's home directory
     */
    public static final File homeDir = new File(System.getProperty("user.home"));
    /**
     * Where we keep our working files
     */
    public static final File workDir = new File(new File(System.getProperty("user.home")), ".jk8sctl");

    /**
     * Get configuration as an object.
     *
     * @return The configuration object.
     */
    public static K8sCtlCfg getConfig() {
        if (null == config) {
            try {
                config = K8sCtlCfg.loadConfig();
            } catch (IOException ex) {
                Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return config;
    }
    /**
     * Global default log object
     */
    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("GLOBAL");

    public static ApiClient getK8sClient() throws IOException {
        if (null == getK8sConfig() || null == getK8sContext()) {
            return Config.defaultClient();
        } else {
            try {
                KubeConfig kCfg = KubeConfig.loadKubeConfig(new FileReader(getK8sConfig()));
                kCfg.setContext(getK8sContext());
                ApiClient c = Config.fromConfig(kCfg);
                return c;
            } catch (IOException ex) {
                int ans = JOptionPane.showConfirmDialog(null, String.format("Cannot connect to %s.%s\nConnect to default cluster instead?", getK8sConfig(), getK8sContext()));
                if (ans == JOptionPane.YES_OPTION) {
                    getConfig().k8sConfig = null;
                    getConfig().k8sContext = null;
                    getConfig().saveConfig();
                    return Config.defaultClient();
                }
                System.exit(0);
            }
        }
        return null;
    }

    public static String getK8sConfig() {
        return getConfig().k8sConfig;
    }

    public static String getK8sContext() {
        return getConfig().k8sContext;
    }

    public static File getYamlDir() {
        File ret = new File(getConfig().yamlDir);
        if (ret.exists()) {
            return ret;
        }
        return new File(System.getProperty("user.home"));
    }

    public static Icon getIcon(String name) {
        String imageLocation = "/icons/" + name + "24.gif";
        URL imageURL = Global.class.getResource(imageLocation);
        return new ImageIcon(imageURL, name);
    }

    public static K8sCtlCfg loadConfig() {
        try {
            config = K8sCtlCfg.loadConfig();
        } catch (IOException ex) {
            Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
        }
        return config;
    }

    public static K8sCtlCfg setConfigFile(String arg) {
        try {
            config = K8sCtlCfg.loadConfig(arg);
        } catch (IOException ex) {
            Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        }
        return config;
    }

    public static void setYamlDir(File parentFile) {
        try {
            getConfig().yamlDir = parentFile.getCanonicalPath();
            getConfig().saveConfig();
        } catch (IOException ex) {
            Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * For simple global warnings
     *
     * @param format printf-like
     * @param parms as specified by the format
     */
    public static void warn(String format, Object... parms) {
        LOG.warn(String.format(format, parms));
    }

    /**
     * Best guess at the kubectl program to use.
     *
     * @return Best guess at the kubectl program to use.
     */
    public static String kubeCtlPrg() {
        return getConfig().kubeCtlProgam;
    }
}
