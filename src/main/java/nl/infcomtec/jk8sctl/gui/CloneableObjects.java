/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl.gui;

import nl.infcomtec.jk8sctl.Maps;

/**
 *
 * @author walter
 */
public class CloneableObjects {

    public static String[] asArray() {
        synchronized (Maps.items) {
            String[] ret = new String[Maps.apps.size()];
            int i = 0;
            for (String s : Maps.apps.keySet()) {
                ret[i++] = s;
            }
            return ret;
        }
    }
}
