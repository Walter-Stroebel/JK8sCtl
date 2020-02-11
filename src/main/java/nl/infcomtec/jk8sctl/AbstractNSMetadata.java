/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1ObjectMeta;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public abstract class AbstractNSMetadata extends AbstractMetadata implements NSMetadata {

    public AbstractNSMetadata(String kind,V1ObjectMeta metadata) {
        super(kind,metadata);
        synchronized (Maps.byNS) {
            List<Metadata> get = Maps.byNS.get(getNamespace());
            if (null == get) {
                Maps.byNS.put(getNamespace(), get = new LinkedList<>());
            }
            get.add(this);
        }
    }

    @Override
    public final String getNamespace() {
        try {
            return metadata.getNamespace();
        } catch (Exception any) {
            return null;
        }
    }

    @Override
    protected TreeMap<String, String> map() {
        TreeMap<String, String> ret = super.map();
        ret.put("namespace", getNamespace());
        return ret;
    }

}
