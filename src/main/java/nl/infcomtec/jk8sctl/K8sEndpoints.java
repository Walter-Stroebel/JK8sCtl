/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Endpoints;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class K8sEndpoints extends AbstractNSMetadata {

    private final V1Endpoints k8s;

    public K8sEndpoints(V1Endpoints k8s) {
        super("endpoints",k8s.getMetadata());
        this.k8s = k8s;
    }

    /**
     * @return the k8s
     */
    public V1Endpoints getK8s() {
        return k8s;
    }

    @Override
    public TreeMap<String, String> map() {
        TreeMap<String, String> ret = super.map();
        return ret;
    }

}
