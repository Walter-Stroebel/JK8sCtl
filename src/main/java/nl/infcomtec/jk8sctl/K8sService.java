/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceSpec;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sService extends AbstractAppReference {

    private final V1Service k8s;

    public K8sService(int mapId, V1Service k8s) {
        super(mapId, "service", k8s.getMetadata());
        this.k8s = k8s;
    }

    /**
     * @return the k8s
     */
    public V1Service getK8s() {
        return k8s;
    }

    @Override
    public String getApp() {
        V1ServiceSpec spec = k8s.getSpec();
        if (null == spec) {
            return null;
        }
        Map<String, String> selector = spec.getSelector();
        if (null == selector) {
            return null;
        }
        if (selector.containsKey("app")) {
            return selector.get("app");
        }
        if (selector.containsKey("k8s-app")) {
            return selector.get("k8s-app");
        }
        return null;
    }

    @Override
    public TreeMap<Integer, K8sRelation> getRelations() {
        TreeMap<Integer, K8sRelation> ret = new TreeMap<>();
        {
            Integer ns = Maps.spaces.get(getNamespace());
            if (null != ns) {
                ret.put(ns, new K8sRelation(false, ns, "ns"));
            }
        }
        String app = getApp();
        if (null != app) {
            for (Metadata md : Maps.items.values()) {
                if (!md.getKind().equals(getKind()) && md.getNamespace().equals(getNamespace()) && md.getName().equals(app)) {                    
                    ret.put(md.getMapId(), new K8sRelation(false, md.getMapId(), "svc"));
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

    @Override
    public DefaultMutableTreeNode getTree() {
        DefaultMutableTreeNode ret = super.getTree();
        dumpBean(k8s.getSpec(), "spec", ret);
        dumpBean(k8s.getStatus(), "status", ret);
        return ret;
    }

}
