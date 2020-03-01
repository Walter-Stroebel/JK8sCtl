/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodTemplateSpec;
import io.kubernetes.client.models.V1ReplicationController;
import io.kubernetes.client.models.V1ReplicationControllerCondition;
import io.kubernetes.client.models.V1ReplicationControllerSpec;
import io.kubernetes.client.models.V1ReplicationControllerStatus;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sReplicationController extends AbstractAppReference {

    private final V1ReplicationController k8s;

    public K8sReplicationController(int mapId, V1ReplicationController k8s) {
        super(mapId, Kinds.replicationcontroller, k8s.getMetadata());
        this.k8s = k8s;
    }

    @Override
    public K8sStatus getStatus() {
        V1ReplicationControllerStatus status = k8s.getStatus();
        if (null==status)return new K8sStatus(false);
        K8sStatus ret = new K8sStatus();
        List<V1ReplicationControllerCondition> conditions = status.getConditions();
        for (V1ReplicationControllerCondition cond:conditions){
            String t = cond.getType();
            String s = cond.getStatus();
            if (s.equals("Unknown")){
                ret.okay=false;
            }
            ret.details.put(t, new K8sCondition(cond));
        }
        return ret;
    }

    @Override
    public V1PodSpec getPodSpec() {
        V1ReplicationControllerSpec spec = k8s.getSpec();
        if (null != spec) {
            V1PodTemplateSpec template = spec.getTemplate();
            if (null != template) {
                return template.getSpec();
            }
        }
        return null;
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
    public V1ReplicationController getK8s() {
        return k8s;
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

}
