/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8salert;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.infcomtec.jk8sctl.CollectorUpdate;
import nl.infcomtec.jk8sctl.Global;
import nl.infcomtec.jk8sctl.K8sCondition;
import nl.infcomtec.jk8sctl.K8sCtlCfg;
import nl.infcomtec.jk8sctl.K8sStatus;
import nl.infcomtec.jk8sctl.Maps;
import nl.infcomtec.jk8sctl.Metadata;
import nl.infcomtec.jk8sctl.gui.Menu;

/**
 *
 * @author walter
 */
public class Start implements CollectorUpdate {

    public static K8sCtlCfg config;
    public static final TreeMap<AlertSinkConfig, Alerter> alerters = new TreeMap<>();

    /**
     * @param args none or valid path to a valid JSON configuration file
     */
    public static void main(String[] args) throws Exception {

        if (null != args && args.length == 1) {
            config = Global.setConfigFile(args[0]);
        } else {
            config = Global.getConfig();
        }
        if (null == config.alerters || config.alerters.length == 0) {
            AlertSinkConfig cfg = new AlertSinkConfig();
            cfg.name = "Console";
            cfg.messagesPerSecond = 3;
            cfg.discardOnNumberQueued = 200;
            cfg.type = "ConsoleAlerter";
            cfg.squelchDuplicates = true;
            config.alerters = new AlertSinkConfig[]{cfg};
            try {
                config.saveConfig();
            } catch (IOException ex) {
                Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(13);
            }
        }
        for (AlertSinkConfig cfg : config.alerters) {
            try {
                Class<Alerter> loadClass = (Class<Alerter>) Start.class.getClassLoader().loadClass("nl.infcomtec.jk8salert." + cfg.type);
                Constructor<?>[] constructors = loadClass.getConstructors();
                alerters.put(cfg, (Alerter) constructors[0].newInstance(cfg));
            } catch (Exception ex) {
                Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Maps.doUpdate(new Start());
        Maps.collect();
        ScheduledExecutorService STE = Executors.newSingleThreadScheduledExecutor();
        STE.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Maps.collect();
                } catch (Exception ex) {
                    Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }, 1, 10, TimeUnit.SECONDS);
        STE.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                synchronized (alerters) {
                    for (Alerter al : alerters.values()) {
                        al.poll();
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean update() {
        for (Metadata item : Maps.items.values()) {
            K8sStatus status = item.getStatus();
            if (!status.okay) {
                for (K8sCondition c : status.details.values()) {
                    if (c.isAnIssue == K8sCondition.Status.True) {
                        synchronized (alerters) {
                            for (Alerter al : alerters.values()) {
                                al.error(String.format("%s %s: %s=%s", item.getKind(), item.getNSName(), c.type, c.status));
                            }
                        }
                    } else {
                        synchronized (alerters) {
                            for (Alerter al : alerters.values()) {
                                al.warning(String.format("%s %s: %s=%s", item.getKind(), item.getNSName(), c.type, c.status));
                            }
                        }
                    }
                }
//            } else {
//                for (K8sCondition c : status.details.values()) {
//                    synchronized (alerters) {
//                        for (Alerter al : alerters.values()) {
//                            al.info(String.format("%s %s: %s=%s", item.getKind(), item.getNSName(), c.type, c.status));
//                        }
//                    }
//                }
            }
        }
        return true;
    }

}
