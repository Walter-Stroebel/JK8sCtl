/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1PodSpec;

/**
 *
 * @author walter
 */
public abstract class AbstractAppReference extends AbstractMetadata {

    public AbstractAppReference(int mapId, Kinds kind, V1ObjectMeta metadata) {
        super(mapId, kind, metadata);
    }

    public abstract String getApp();

    public abstract V1PodSpec getPodSpec();
}
