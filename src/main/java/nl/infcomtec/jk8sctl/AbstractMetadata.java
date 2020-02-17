/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1ObjectMeta;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public abstract class AbstractMetadata implements Metadata {

    private final int mapId;

    protected final V1ObjectMeta metadata;
    private final String kind;

    public AbstractMetadata(int mapId, String kind, V1ObjectMeta metadata) {
        this.mapId = mapId;
        this.metadata = metadata;
        this.kind = kind;
    }

    @Override
    public final DateTime getCreationTimestamp() {
        try {
            return metadata.getCreationTimestamp();
        } catch (Exception any) {
            return new DateTime();
        }
    }

    @Override
    public final String getName() {
        try {
            return metadata.getName();
        } catch (Exception any) {
            return null;
        }
    }

    @Override
    public final UUID getUUID() {
        try {
            return UUID.fromString(metadata.getUid());
        } catch (Exception any) {
            return null;
        }
    }

    @Override
    public final String getUid() {
        return metadata.getUid();
    }

    /**
     * @return the kind
     */
    @Override
    public final String getKind() {
        return kind;
    }

    /**
     * @return the mapId
     */
    @Override
    public int getMapId() {
        return mapId;
    }

    @Override
    public final String getNamespace() {
        if (null == metadata) {
            return null;
        }
        if (null == metadata.getNamespace()) {
            return "";
        }
        return metadata.getNamespace();
    }

    @Override
    public final String getNSName() {
        return getNamespace() + "." + getName();
    }

    @Override
    public String getDotNodeName() {
        return "nd" + mapId;
    }

    public DefaultMutableTreeNode metadataNode() {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode("metadata");
        ret.add(new DefaultMutableTreeNode(String.format("creationTimestamp=%1$tF %1$tT", getCreationTimestamp().getMillis())));
        dumpMap(metadata.getAnnotations(), "annotations", ret);
        dumpMap(metadata.getLabels(), "labels", ret);
        return ret;
    }

    @Override
    public DefaultMutableTreeNode getTree() {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(String.format("%s=%s", getKind(), getName()));
        ret.add(metadataNode());
        return ret;
    }

    protected void dumpBean(Object obj, String what, DefaultMutableTreeNode ret) {
        if (null != obj) {
            DefaultMutableTreeNode n = new DefaultMutableTreeNode(what);
            for (Method m : obj.getClass().getDeclaredMethods()) {
                if (m.getName().startsWith("get") && m.getParameterCount() == 0) {
                    try {
                        Object o = m.invoke(obj);
                        if (null != o) {
                            DefaultMutableTreeNode k = new DefaultMutableTreeNode(m.getName().substring(3));
                            if (o instanceof List) {
                                for (Object o2:(List)o) {
                                    dumpBean(o2, o2.getClass().getSimpleName(), k);
                                }
                            } else if (o.getClass().getSimpleName().startsWith("V1")) {
                                dumpBean(o, o.getClass().getSimpleName().substring(2), k);
                            } else {
                                k.add(new DefaultMutableTreeNode(o.toString()));
                            }
                            n.add(k);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(AbstractMetadata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (m.getName().startsWith("is") && m.getParameterCount() == 0) {
                    try {
                        Object o = m.invoke(obj);
                        if (null != o) {
                            DefaultMutableTreeNode k = new DefaultMutableTreeNode(m.getName().substring(2));
                            k.add(new DefaultMutableTreeNode(o.toString()));
                            n.add(k);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(AbstractMetadata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            ret.add(n);
        }
    }

    protected void dumpMap(Map<String, String> map, String what, DefaultMutableTreeNode ret) {
        if (null != map && !map.isEmpty()) {
            DefaultMutableTreeNode n = new DefaultMutableTreeNode(what);
            for (Map.Entry<String, String> e : map.entrySet()) {
                DefaultMutableTreeNode k = new DefaultMutableTreeNode(e.getKey());
                k.add(new DefaultMutableTreeNode(e.getValue()));
                n.add(k);
            }
            ret.add(n);
        }
    }

    protected StringBuilder pre() {
        StringBuilder ret = new StringBuilder();
        ret.append(getDotNodeName()).append("[label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">\n");
        ret.append("<TR><TD>").append(getKind()).append("</TD><TD>").append(getName()).append("</TD></TR>");
        return ret;
    }

    @Override
    public String toString() {
        if (kind.equals("namespace")) return kind+":"+getName();
        else return kind+":"+getNSName();
    }

    protected void post(StringBuilder sb) {
        sb.append("</TABLE>>];\n");
    }
}
