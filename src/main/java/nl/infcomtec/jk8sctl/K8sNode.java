/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1ContainerImage;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeAddress;
import io.kubernetes.client.models.V1NodeStatus;
import java.util.Map;
import java.util.TreeMap;
import nl.infcomtec.basicutils.ShowValues;

/**
 *
 * @author walter
 */
public class K8sNode extends AbstractMetadata {

    private V1Node k8s;

    @Override
    public TreeMap<String, String> map() {
        TreeMap<String, String> ret = super.map();
        V1NodeStatus status = k8s.getStatus();
        if (null != status) {
            if (null != status.getAddresses()) {
                for (V1NodeAddress a : status.getAddresses()) {
                    ret.put(a.getType(), a.getAddress());
                }
            }
            if (null != status.getCapacity()) {
                for (Map.Entry<String, Quantity> e : status.getCapacity().entrySet()) {
                    switch (e.getKey()) {
                        case "ephemeral-storage":
                        case "memory":
                            ret.put(e.getKey(), ShowValues.humanLongDig5KMGT(e.getValue().getNumber().longValue()).trim());
                            break;
                        default:
                            ret.put(e.getKey(), e.getValue().toSuffixedString());
                            break;
                    }
                }
            }
            if (null != status.getImages()) {
                ret.put("totalImageSize", ShowValues.humanLongDig5KMGT(getTotalImageSize()).trim());
            }
        }
        return ret;
    }

    public K8sNode(V1Node k8s) {
        super("node",k8s.getMetadata());
        this.k8s = k8s;
    }

    public long getTotalImageSize() {
        try {
            long iSize = 0;
            for (V1ContainerImage e : k8s.getStatus().getImages()) {
                if (null != e.getSizeBytes()) {
                    iSize += e.getSizeBytes();
                }
            }
            return iSize;
        } catch (Exception any) {
            return 0;
        }
    }

    /**
     * @return the k8s
     */
    public V1Node getK8s() {
        return k8s;
    }

    /**
     * @param k8s the k8s to set
     */
    public void setK8s(V1Node k8s) {
        this.k8s = k8s;
    }
}
