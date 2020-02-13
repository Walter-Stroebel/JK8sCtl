/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1ObjectMeta;
import java.util.UUID;
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
        return getNamespace()+"."+getName();
    }

    @Override
    public String getDotNodeName() {
        return "nd" + mapId;
    }

    protected StringBuilder pre() {
        StringBuilder ret = new StringBuilder();
        ret.append(getDotNodeName()).append("[label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">\n");
        ret.append("<TR><TD>").append(getKind()).append("</TD><TD>").append(getName()).append("</TD></TR>");
        return ret;
    }

    protected void post(StringBuilder sb) {
        sb.append("</TABLE>>];\n");
    }
}
