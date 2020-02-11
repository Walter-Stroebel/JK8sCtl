/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds all global data and configuration.
 *
 * @author walter
 */
public class Global {

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
    public static final File workDir = new File(new File(System.getProperty("user.home")), ".k8sctl");

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

    public static K8sCtlCfg loadConfig() {
        try {
            config = K8sCtlCfg.loadConfig();
        } catch (IOException ex) {
            Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
        }
        return config;
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

    public static String kubeCtlPrg() {
        return getConfig().kubeCtlProgam;
    }
}
