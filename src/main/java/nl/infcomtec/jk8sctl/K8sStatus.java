/*
 *  Copyright (c) 2020 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class K8sStatus {
    public boolean okay = true;
    public final TreeMap<String,K8sCondition> details = new TreeMap<>();
    public K8sStatus(boolean okay) {
        this.okay = okay;
    }

    public K8sStatus() {
    }
    
}
