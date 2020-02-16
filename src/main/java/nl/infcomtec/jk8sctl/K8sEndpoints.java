/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1EndpointAddress;
import io.kubernetes.client.models.V1EndpointPort;
import io.kubernetes.client.models.V1EndpointSubset;
import io.kubernetes.client.models.V1Endpoints;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sEndpoints extends AbstractAppReference {

    private final V1Endpoints k8s;

    public K8sEndpoints(int mapId, V1Endpoints k8s) {
        super(mapId, "endpoints", k8s.getMetadata());
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
                    ret.put(md.getMapId(), new K8sRelation(true, md.getMapId(), "endp"));
                }
            }
        }
        return ret;
    }

    @Override
    public DefaultMutableTreeNode getTree() {
        DefaultMutableTreeNode ret = super.getTree();
        List<V1EndpointSubset> subsets = k8s.getSubsets();
        if (null != subsets && !subsets.isEmpty()) {
            for (V1EndpointSubset e : subsets) {
                DefaultMutableTreeNode subset = new DefaultMutableTreeNode("subset");
                ret.add(subset);
                {
                    List<V1EndpointAddress> al = e.getAddresses();
                    if (null != al && !al.isEmpty()) {
                        for (V1EndpointAddress a : al) {
                            dumpBean(a, "addresses", subset);
                        }
                    }
                }
                {
                    List<V1EndpointAddress> al = e.getNotReadyAddresses();
                    if (null != al && !al.isEmpty()) {
                        for (V1EndpointAddress a : al) {
                            dumpBean(a, "notReadyAddresses", subset);
                        }
                    }
                }
                {
                    List<V1EndpointPort> pl = e.getPorts();
                    if (null != pl && !pl.isEmpty()) {
                        for (V1EndpointPort a : pl) {
                            dumpBean(a, "ports", subset);
                        }
                    }
                }
            }
        }
        return ret;
    }

}
