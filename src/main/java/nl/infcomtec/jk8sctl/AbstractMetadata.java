/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1ObjectMeta;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import nl.infcomtec.basicutils.ShowValues;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public abstract class AbstractMetadata implements Metadata {

    protected final V1ObjectMeta metadata;
    private final String kind;
    
    public AbstractMetadata(String kind, V1ObjectMeta metadata) {
        this.metadata=metadata;
        this.kind=kind;
        if (null != metadata) {
            synchronized (Maps.byUUID){
                List<Metadata> get = Maps.byUUID.get(getUUID());
                if (null==get){
                    Maps.byUUID.put(getUUID(), get=new LinkedList<>());
                }
                get.add(this);
            }
            synchronized (Maps.byNAME){
                List<Metadata> get = Maps.byNAME.get(getName());
                if (null==get){
                    Maps.byNAME.put(getName(), get=new LinkedList<>());
                }
                get.add(this);
            }
        }
    }

    protected TreeMap<String, String> map() {
        TreeMap<String, String> ret = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        ret.put("kind", kind);
        if (null == metadata) {
            ret.put("name", "null");
            return ret;
        }
        ret.put("uid", getUid());
        ret.put("name", getName());
        ret.put("age", ShowValues.elaspedFromMillis(System.currentTimeMillis() - getCreationTimestamp().getMillis()).trim());
        return ret;
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
    @Override
    public String toString() {
        return map().toString();
    }

    /**
     * @return the kind
     */
    @Override
    public final String getKind() {
        return kind;
    }
}
