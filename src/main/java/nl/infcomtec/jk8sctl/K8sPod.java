/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Pod;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class K8sPod extends AbstractMetadata {

    private final V1Pod k8s;

    public K8sPod(int mapId,V1Pod k8s) {
        super(mapId,"pod",k8s.getMetadata());
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

    public String getApp() {        
        Map<String, String> lbs = k8s.getMetadata().getLabels();
        if (null == lbs) {
            return null;
        }
        if (lbs.containsKey("app")) {
            return lbs.get("app");
        }
        if (lbs.containsKey("k8s-app")) {
            return lbs.get("k8s-app");
        }
        return null;
    }
    
    @Override
    public TreeMap<Integer, String> getRelations() {
        TreeMap<Integer,String> ret = new TreeMap<>();
        Integer get = Maps.spaces.get(getNamespace());
        if (null!=get){
            ret.put(get, "ns");
        }
        for (Metadata md : Maps.items.values()){
            if ("node".equals(md.getKind())&&getNodeName().equals(md.getName())){
                ret.put(md.getMapId(), "node");
            }
        }
        String app = getApp();
        if (null != app) {
            for (Metadata md : Maps.items.values()) {
                if (!md.getKind().equals(getKind()) && md.getNamespace().equals(getNamespace()) && md.getName().equals(app)) {
                    ret.put(md.getMapId(), "pod");
                }
            }
        }
        return ret;
    }

    @Override
    public StringBuilder getDotNode() {
        StringBuilder ret = pre();
        post(ret);
        return ret;
    }

}
