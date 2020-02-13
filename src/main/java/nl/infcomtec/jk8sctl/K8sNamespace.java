/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Namespace;
import java.util.TreeMap;

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
    public TreeMap<Integer, String> getRelations() {
        TreeMap<Integer, String> ret = new TreeMap<>();
        if (getMapId()!=0){
            ret.put(0, "");
        }
        return ret;
    }

}
