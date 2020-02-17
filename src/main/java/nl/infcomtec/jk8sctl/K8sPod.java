/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Pod;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sPod extends AbstractAppReference {

    private final V1Pod k8s;

    public K8sPod(int mapId, V1Pod k8s) {
        super(mapId, "pod", k8s.getMetadata());
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

    @Override
    public String getApp() {
        Map<String, String> lbs = k8s.getMetadata().getLabels();
        if (null == lbs) {
            // simple named pod
            return getName();
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
                    ret.put(md.getMapId(), new K8sRelation(false, md.getMapId(), md.getKind()));
                }
            }
        }
        if (null != getNodeName()) {
            String nn = getNodeName();
            for (Metadata md : Maps.items.values()) {
                if (md.getKind().equals("node") && md.getName().equals(nn)) {
                    ret.put(md.getMapId(), new K8sRelation(false, md.getMapId(), md.getKind()));
                }
            }
        }
        return ret;
    }

    @Override
    public StringBuilder getDotNode() {
        StringBuilder ret = pre();
        if (null!=getApp())
        ret.append("<TR><TD>").append("app").append("</TD><TD>").append(getApp()).append("</TD></TR>");
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
