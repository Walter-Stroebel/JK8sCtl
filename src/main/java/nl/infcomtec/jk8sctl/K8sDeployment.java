/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentCondition;
import io.kubernetes.client.models.V1DeploymentSpec;
import io.kubernetes.client.models.V1DeploymentStatus;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodTemplateSpec;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sDeployment extends AbstractAppReference {

    private final V1Deployment k8s;

    public K8sDeployment(int mapId, V1Deployment k8s) {
        super(mapId, Kinds.deployment, k8s.getMetadata());
        this.k8s = k8s;
    }

    @Override
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
    public StringBuilder getDotNode() {
        StringBuilder ret = pre();
        post(ret);
        return ret;
    }

    @Override
    public V1PodSpec getPodSpec() {
        V1DeploymentSpec spec = k8s.getSpec();
        if (null != spec) {
            V1PodTemplateSpec template = spec.getTemplate();
            if (null != template) {
                return template.getSpec();
            }
        }
        return null;
    }

    @Override
    public TreeMap<Integer, K8sRelation> getRelations() {
        TreeMap<Integer, K8sRelation> ret = new TreeMap<>();
        return ret;
    }

    @Override
    public K8sStatus getStatus() {
        V1DeploymentStatus status = k8s.getStatus();
        if (null == status) {
            return new K8sStatus(false);
        }
        int actRep = null==status.getReplicas()?0:status.getReplicas();
        K8sStatus ret = new K8sStatus();
        List<V1DeploymentCondition> conditions = status.getConditions();
        for (V1DeploymentCondition cond : conditions) {
            String t = cond.getType();
            String s = cond.getStatus();
            K8sCondition kc = new K8sCondition(cond);
            if (s.equals("Unknown")) {
                ret.okay = false;
                kc.isAnIssue=K8sCondition.Status.True;
            } else if (t.equals("Progressing") && !s.equals("True")) {
                ret.okay = false;
                kc.isAnIssue=K8sCondition.Status.True;
            } else if (t.equals("Progressing") && s.equals("True")) {
                kc.isAnIssue=K8sCondition.Status.False;
            } else if (t.endsWith("Available") && !s.equals("True")) {
                ret.okay = false;
                kc.isAnIssue=K8sCondition.Status.True;
            } else if (t.endsWith("Available") && s.equals("True")) {
                kc.isAnIssue=K8sCondition.Status.False;
            }
            ret.details.put(t, kc);
        }
        if (null == k8s.getSpec()) {
            ret.okay = false;
        } else {
            int desRep = k8s.getSpec().getReplicas();
            if (actRep != desRep) {
                ret.okay = false;
                ret.details.put("Replicas", new K8sCondition(String.format("Replicas %d of %d",actRep,desRep), K8sCondition.Status.False,K8sCondition.Status.True));
            } else {
                ret.details.put("Replicas", new K8sCondition(String.format("Replicas %d of %d",actRep,desRep), K8sCondition.Status.True,K8sCondition.Status.False));
            }
        }
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
     * @return the k8s
     */
    public V1Deployment getK8s() {
        return k8s;
    }

}
