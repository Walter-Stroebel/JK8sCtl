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
public class K8sDeployment extends AbstractMetadata {

    private final V1Deployment k8s;

    public K8sDeployment(int mapId, V1Deployment k8s) {
        super(mapId, "deployment", k8s.getMetadata());
        this.k8s = k8s;
    }

    @Override
    public StringBuilder getDotNode() {
        StringBuilder ret = pre();
        post(ret);
        return ret;
    }

    /**
     * @return the k8s
     */
    public V1Deployment getK8s() {
        return k8s;
    }
    
    @Override
    public TreeMap<Integer, String> getRelations() {
        TreeMap<Integer,String> ret = new TreeMap<>();
        Integer get = Maps.spaces.get(getNamespace());
        if (null!=get){
            ret.put(get, "ns");
        }
        return ret;
    }

}
