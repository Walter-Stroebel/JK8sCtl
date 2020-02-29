/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public interface Metadata extends Comparable<Metadata>{

    K8sStatus getStatus();

    /**
     *
     * @return the kind of object
     */
    String getKind();

    /**
     * @return the creationTimestamp
     */
    DateTime getCreationTimestamp();

    /**
     * @return the uid
     */
//    UUID getUUID();

    /**
     *
     * @return the UUID as a string
     */
//    String getUid();

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the namespace dot name
     */
    String getNSName();

    /**
     * @return GraphViz node
     */
    StringBuilder getDotNode();

    /**
     * @return GraphViz node name
     */
    String getDotNodeName();

    /**
     * @return the namespace. Empty string if the item has no namespace
     */
    String getNamespace();

    /**
     * @return the mapId
     */
    int getMapId();

    /**
     * For GraphViz: other objects this object relates to.
     *
     * @return edges
     */
    TreeMap<Integer, K8sRelation> getRelations();

    /**
     * For tree display
     *
     * @return root of this (sub) tree
     */
    DefaultMutableTreeNode getTree();
}
