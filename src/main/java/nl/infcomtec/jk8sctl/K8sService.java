/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceSpec;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sService extends AbstractAppReference {

    private final V1Service k8s;

    public K8sService(int mapId, V1Service k8s) {
        super(mapId, Kinds.service, k8s.getMetadata());
        this.k8s = k8s;
    }

    /**
     * @return the k8s
     */
    public V1Service getK8s() {
        return k8s;
    }

    @Override
    public K8sStatus getStatus() {
        return new K8sStatus();
    }

    @Override
    public V1PodSpec getPodSpec() {
        if (null!=getApp()){
            TreeSet<Integer> get = Maps.apps.get(getApp());
            for (int sp:get){
                Metadata item = Maps.items.get(sp);
                if (null!=item){
                    if (item instanceof K8sDeployment) {
                        return ((K8sDeployment) item).getPodSpec();
                    }
                    if (item instanceof K8sReplicationController) {
                        return ((K8sReplicationController) item).getPodSpec();
                    }
                    if (item instanceof K8sPod) {
                        return ((K8sPod) item).getPodSpec();
                    }
                }
            }
        }
        return null;
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
