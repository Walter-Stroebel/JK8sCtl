/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Pod;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class K8sPod extends AbstractNSMetadata {

    private final V1Pod k8s;

    public K8sPod(V1Pod k8s) {
        super("pod",k8s.getMetadata());
        this.k8s = k8s;
    }

    /**
     * @return the k8s
     */
    public V1Pod getK8s() {
        return k8s;
    }

    public String getNodeName() {
        try {
            return k8s.getSpec().getNodeName();
        } catch (Exception any) {
            return null;
        }
    }

    public K8sNode getMyNode() {
        try {
            return (K8sNode) Maps.byNAME.get(getNodeName());
        } catch (Exception any) {
            return null;
        }
    }

    @Override
    public TreeMap<String, String> map() {
        TreeMap<String, String> ret = super.map();
        return ret;
    }

}
