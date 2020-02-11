/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Service;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class K8sService extends AbstractNSMetadata {

    private final V1Service k8s;

    public K8sService(V1Service k8s) {
        super("service",k8s.getMetadata());
        this.k8s = k8s;
    }

    /**
     * @return the k8s
     */
    public V1Service getK8s() {
        return k8s;
    }

    @Override
    public TreeMap<String, String> map() {
        TreeMap<String, String> ret = super.map();
        return ret;
    }

}
