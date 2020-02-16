/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1ObjectMeta;

/**
 *
 * @author walter
 */
public abstract class AbstractAppReference extends AbstractMetadata {
    
    public AbstractAppReference(int mapId, String kind, V1ObjectMeta metadata) {
        super(mapId, kind, metadata);
    }
    public abstract String getApp();
}
