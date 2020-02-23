/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodStatus;
import io.kubernetes.client.models.V1ResourceRequirements;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import static nl.infcomtec.jk8sctl.Global.GiBf;

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
        if (null == status) {
            return new K8sStatus(false);
        }
        K8sStatus ret = new K8sStatus();
        List<V1PodCondition> conditions = status.getConditions();
        for (V1PodCondition cond : conditions) {
            String t = cond.getType();
            String s = cond.getStatus();
            K8sCondition kc = new K8sCondition(cond);
            if (!s.equals("True")) {
                ret.okay = false;
                kc.isAnIssue = K8sCondition.Status.True;
            } else {
                kc.isAnIssue = K8sCondition.Status.False;
            }
            ret.details.put(t, kc);
        }
        if (!getNamespace().equals("kube-system")) {
            // We will assume the Kubernetes team knows what they are doing
            // in their own namespace and ignore the missing resource
            // specification. Everybody else is fair game.
            try {
                List<V1Container> containers = k8s.getSpec().getContainers();
                for (V1Container c : containers) {
                    V1ResourceRequirements resources = c.getResources();
                    Map<String, Quantity> limits = resources.getLimits();
                    if (null == limits) {
                        ret.okay = false;
                        K8sCondition kc = new K8sCondition("Limits", K8sCondition.Status.Unknown, K8sCondition.Status.True);
                        ret.details.put("Limits", kc);
                    }
                    Map<String, Quantity> requests = resources.getRequests();
                    if (null == requests) {
                        ret.okay = false;
                        K8sCondition kc = new K8sCondition("Requests", K8sCondition.Status.Unknown, K8sCondition.Status.True);
                        ret.details.put("Requests", kc);
                    }
                }
            } catch (Exception any) {
                // we tried
            }
        }
        return ret;
    }

    /**
     * From limits and requests - if filled in!
     * <p>
     * I must say ... this really feels unfinished. I am probably missing
     * something but this can't be how Kubernetes manages resources, can it?
     *
     * @return limits is mapped as "avail", requests is mapped as "used"
     */
    public K8sResources getResources() {
        K8sResources ret = new K8sResources();
        try {
            List<V1Container> containers = k8s.getSpec().getContainers();
            for (V1Container c : containers) {
                V1ResourceRequirements resources = c.getResources();
                Map<String, Quantity> limits = resources.getLimits();
                try {
                    ret.cpuAvail = limits.get("cpu").getNumber().doubleValue();
                    ret.memAvail = limits.get("memory").getNumber().doubleValue();
                } catch (Exception nullPointer) {
                    // sane maxima
                    ret.cpuAvail = 1;
                    ret.memAvail = GiBf;
                }
                Map<String, Quantity> requests = resources.getRequests();
                try {
                    ret.cpuUsed = requests.get("cpu").getNumber().doubleValue();
                    ret.memUsed = requests.get("memory").getNumber().doubleValue();
                } catch (Exception nullPointer) {
                    // sane minima
                    ret.cpuAvail = 0.1;
                    ret.memAvail = GiBf * 0.1;
                }
            }
        } catch (Exception any) {
            // we tried
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
        if (null != getApp()) {
            ret.append("<TR><TD>").append("app").append("</TD><TD>").append(getApp()).append("</TD></TR>");
        }
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
