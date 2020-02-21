/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodStatus;
import java.util.List;
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

    @Override
    public K8sStatus getStatus() {
        V1PodStatus status = k8s.getStatus();
        if (null==status)return new K8sStatus(false);
        K8sStatus ret = new K8sStatus();
        List<V1PodCondition> conditions = status.getConditions();
        for (V1PodCondition cond:conditions){
            String t = cond.getType();
            String s = cond.getStatus();
            if (!s.equals("True")){
                ret.okay=false;
            }
            ret.details.put(t, new K8sCondition(cond));
        }
        return ret;
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
    public V1PodSpec getPodSpec() {
        return k8s.getSpec();
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
