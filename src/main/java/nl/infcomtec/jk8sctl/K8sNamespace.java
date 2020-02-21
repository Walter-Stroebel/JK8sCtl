/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Namespace;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class K8sNamespace extends AbstractMetadata {

    public K8sNamespace(int mapId, V1Namespace metadata) {
        super(mapId, "namespace", metadata.getMetadata());
    }

    @Override
    public StringBuilder getDotNode() {
        StringBuilder ret = pre();
        post(ret);
        return ret;
    }

    @Override
    public K8sStatus getStatus() {
        return new K8sStatus();
    }

    @Override
    public TreeMap<Integer, K8sRelation> getRelations() {
        TreeMap<Integer, K8sRelation> ret = new TreeMap<>();
        if (getMapId() != 0) {
            ret.put(0, new K8sRelation(false, 0, ""));
        }
        return ret;
    }

    @Override
    public DefaultMutableTreeNode getTree() {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(getKind());
        ret.add(new DefaultMutableTreeNode(getName()));
        ret.add(new DefaultMutableTreeNode(getCreationTimestamp()));
        if (getMapId() == 0) {
            for (Metadata item : Maps.items.values()) {
                if (item.getMapId() == 0) {
                    continue;
                }
                if (item instanceof K8sNamespace) {
                    ret.add(item.getTree());
                }
                if (item instanceof K8sNode) {
                    ret.add(item.getTree());
                }
            }
        } else {
            for (Metadata item : Maps.items.values()) {
                if (!(item instanceof K8sNamespace)) {
                    if (getName().equals(item.getNamespace())) {
                        ret.add(item.getTree());
                    }
                }
            }
        }
        return ret;
    }

}
