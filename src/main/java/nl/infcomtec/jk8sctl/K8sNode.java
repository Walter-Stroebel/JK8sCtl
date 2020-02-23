/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1ContainerImage;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeCondition;
import io.kubernetes.client.models.V1NodeSpec;
import io.kubernetes.client.models.V1NodeStatus;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sNode extends AbstractMetadata {

    private V1Node k8s;

    public K8sNode(int mapId, V1Node k8s) {
        super(mapId, "node", k8s.getMetadata());
        this.k8s = k8s;
    }

    public K8sResources getResources() {
        K8sResources ret = new K8sResources();
        try {
            for (Map.Entry<String, Quantity> e : k8s.getStatus().getAllocatable().entrySet()) {
                switch (e.getKey()) {
                    case "cpu":
                        ret.cpuAvail = e.getValue().getNumber().doubleValue();
                        break;
                    case "ephemeral-storage":
                        ret.dskAvail = e.getValue().getNumber().doubleValue();
                        break;
                    case "memory":
                        ret.memAvail = e.getValue().getNumber().doubleValue();
                        break;
                    case "pods":
                        ret.podAvail = e.getValue().getNumber().doubleValue();
                        break;
                }
            }
            for (V1ContainerImage e : k8s.getStatus().getImages()) {
                ret.dskUsed += e.getSizeBytes();
            }
        } catch (Exception any) {
            Global.warn("Failed to collect node resources %s", any.toString());
        }
        return ret;
    }

    @Override
    public K8sStatus getStatus() {
        V1NodeStatus status = k8s.getStatus();
        if (null == status) {
            return new K8sStatus(false);
        }
        K8sStatus ret = new K8sStatus();
        List<V1NodeCondition> conditions = status.getConditions();
        for (V1NodeCondition cond : conditions) {
            String t = cond.getType();
            String s = cond.getStatus();
            K8sCondition kc = new K8sCondition(cond);
            if (s.equals("Unknown")) {
                ret.okay = false;
                kc.isAnIssue = K8sCondition.Status.True;
            } else if (t.equals("Ready") && !s.equals("True")) {
                ret.okay = false;
                kc.isAnIssue = K8sCondition.Status.True;
            } else if (t.equals("Ready") && s.equals("True")) {
                kc.isAnIssue = K8sCondition.Status.False;
            } else if (t.endsWith("Pressure")) {
                if (s.equals("True")) {
                    ret.okay = false;
                    kc.isAnIssue = K8sCondition.Status.True;
                } else {
                    kc.isAnIssue = K8sCondition.Status.False;
                }
            }
            ret.details.put(t, kc);
        }
        V1NodeSpec spec = k8s.getSpec();
        if (null == spec) {
            ret.okay = false;
            ret.details.put("node.spec", new K8sCondition("node.spec", K8sCondition.Status.Unknown, K8sCondition.Status.True));
        } else {
            if (null != spec.isUnschedulable() && spec.isUnschedulable()) {
                ret.okay = false;
                ret.details.put("Unschedulable", new K8sCondition("Unschedulable", K8sCondition.Status.True, K8sCondition.Status.False));
            }
        }
        return ret;
    }

    public long getTotalImageSize() {
        try {
            long iSize = 0;
            for (V1ContainerImage e : k8s.getStatus().getImages()) {
                if (null != e.getSizeBytes()) {
                    iSize += e.getSizeBytes();
                }
            }
            return iSize;
        } catch (Exception any) {
            return 0;
        }
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
    public V1Node getK8s() {
        return k8s;
    }

    @Override
    public TreeMap<Integer, K8sRelation> getRelations() {
        TreeMap<Integer, K8sRelation> ret = new TreeMap<>();
        return ret;
    }

    @Override
    public DefaultMutableTreeNode getTree() {
        DefaultMutableTreeNode ret = super.getTree();
        dumpBean(k8s.getSpec(), "spec", ret);
        dumpBean(k8s.getStatus(), "status", ret);
        return ret;
    }

    /**
     * @param k8s the k8s to set
     */
    public void setK8s(V1Node k8s) {
        this.k8s = k8s;
    }
}
