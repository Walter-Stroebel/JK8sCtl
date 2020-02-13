/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Endpoints;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class K8sEndpoints extends AbstractMetadata {

    private final V1Endpoints k8s;

    public K8sEndpoints(int mapId,V1Endpoints k8s) {
        super(mapId,"endpoints",k8s.getMetadata());
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
    public V1Endpoints getK8s() {
        return k8s;
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
        String app = getApp();
        if (null != app) {
            for (Metadata md : Maps.items.values()) {
                if (!md.getKind().equals(getKind()) && md.getNamespace().equals(getNamespace()) && md.getName().equals(app)) {
                    ret.put(md.getMapId(), "endp");
                }
            }
        }
        return ret;
    }

}
