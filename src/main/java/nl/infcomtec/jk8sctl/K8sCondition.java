/*
 *  Copyright (c) 2020 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1DeploymentCondition;
import io.kubernetes.client.models.V1NodeCondition;
import io.kubernetes.client.models.V1PodCondition;
import io.kubernetes.client.models.V1ReplicationControllerCondition;
import nl.infcomtec.basicutils.ShowValues;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public class K8sCondition {

    public final String type;

    public enum Status {
        True, False, Unknown
    };
    public final Status status;
    public final String lastTransitionAge;
    public final String lastUpdateAge;
    public final long lastTransitionTime;
    public final long lastUpdateTime;

    public K8sCondition(String type, Status status) {
        this.type = type;
        this.status = status;
        this.lastTransitionAge = "Unknown";
        this.lastTransitionTime = 0;
        this.lastUpdateAge = "Unknown";
        this.lastUpdateTime = 0;
    }

    public K8sCondition(V1PodCondition c) {
        type = c.getType();
        if (null == c.getStatus()) {
            status = Status.Unknown;
        } else {
            switch (c.getStatus().toLowerCase()) {
                case "false":
                    status = Status.False;
                    break;
                case "true":
                    status = Status.True;
                    break;
                default:
                    status = Status.Unknown;
                    break;
            }
        }
        {
            DateTime dt = c.getLastTransitionTime();
            if (null == dt) {
                lastTransitionAge = "Unknown";
                lastTransitionTime = 0;
            } else {
                lastTransitionTime = dt.getMillis();
                lastTransitionAge = ShowValues.elaspedFromMillis(System.currentTimeMillis() - lastTransitionTime);
            }
        }
        {
            DateTime dt = c.getLastProbeTime();
            if (null == dt) {
                lastUpdateAge = "Unknown";
                lastUpdateTime = 0;
            } else {
                lastUpdateTime = dt.getMillis();
                lastUpdateAge = ShowValues.elaspedFromMillis(System.currentTimeMillis() - lastUpdateTime);
            }
        }
    }

    public K8sCondition(V1NodeCondition c) {
        type = c.getType();
        if (null == c.getStatus()) {
            status = Status.Unknown;
        } else {
            switch (c.getStatus().toLowerCase()) {
                case "false":
                    status = Status.False;
                    break;
                case "true":
                    status = Status.True;
                    break;
                default:
                    status = Status.Unknown;
                    break;
            }
        }
        {
            DateTime dt = c.getLastTransitionTime();
            if (null == dt) {
                lastTransitionAge = "Unknown";
                lastTransitionTime = 0;
            } else {
                lastTransitionTime = dt.getMillis();
                lastTransitionAge = ShowValues.elaspedFromMillis(System.currentTimeMillis() - lastTransitionTime);
            }
        }
        {
            DateTime dt = c.getLastHeartbeatTime();
            if (null == dt) {
                lastUpdateAge = "Unknown";
                lastUpdateTime = 0;
            } else {
                lastUpdateTime = dt.getMillis();
                lastUpdateAge = ShowValues.elaspedFromMillis(System.currentTimeMillis() - lastUpdateTime);
            }
        }
    }

    public K8sCondition(V1DeploymentCondition c) {
        type = c.getType();
        if (null == c.getStatus()) {
            status = Status.Unknown;
        } else {
            switch (c.getStatus().toLowerCase()) {
                case "false":
                    status = Status.False;
                    break;
                case "true":
                    status = Status.True;
                    break;
                default:
                    status = Status.Unknown;
                    break;
            }
        }
        {
            DateTime dt = c.getLastTransitionTime();
            if (null == dt) {
                lastTransitionAge = "Unknown";
                lastTransitionTime = 0;
            } else {
                lastTransitionTime = dt.getMillis();
                lastTransitionAge = ShowValues.elaspedFromMillis(System.currentTimeMillis() - lastTransitionTime);
            }
        }
        {
            DateTime dt = c.getLastUpdateTime();
            if (null == dt) {
                lastUpdateAge = "Unknown";
                lastUpdateTime = 0;
            } else {
                lastUpdateTime = dt.getMillis();
                lastUpdateAge = ShowValues.elaspedFromMillis(System.currentTimeMillis() - lastUpdateTime);
            }
        }
    }

    public K8sCondition(V1ReplicationControllerCondition c) {
        type = c.getType();
        if (null == c.getStatus()) {
            status = Status.Unknown;
        } else {
            switch (c.getStatus().toLowerCase()) {
                case "false":
                    status = Status.False;
                    break;
                case "true":
                    status = Status.True;
                    break;
                default:
                    status = Status.Unknown;
                    break;
            }
        }
        {
            DateTime dt = c.getLastTransitionTime();
            if (null == dt) {
                lastTransitionAge = "Unknown";
                lastTransitionTime = 0;
            } else {
                lastTransitionTime = dt.getMillis();
                lastTransitionAge = ShowValues.elaspedFromMillis(System.currentTimeMillis() - lastTransitionTime);
            }
        }
        lastUpdateAge = "Unknown";
        lastUpdateTime = 0;
    }

}
