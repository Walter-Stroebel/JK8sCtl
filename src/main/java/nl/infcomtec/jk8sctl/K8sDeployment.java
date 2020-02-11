/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Deployment;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class K8sDeployment extends AbstractNSMetadata {

    private final V1Deployment k8s;

    public K8sDeployment(V1Deployment k8s) {
        super("deployment",k8s.getMetadata());
        this.k8s = k8s;
    }

    /**
     * @return the k8s
     */
    public V1Deployment getK8s() {
        return k8s;
    }

    @Override
    public TreeMap<String, String> map() {
        TreeMap<String, String> ret = super.map();
        return ret;
    }

}
