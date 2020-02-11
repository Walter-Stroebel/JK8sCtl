/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.util.UUID;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public interface Metadata {
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
    UUID getUUID();

    /**
     *
     * @return the UUID as a string
     */
    String getUid();

    /**
     * @return the name
     */
    String getName();
}
