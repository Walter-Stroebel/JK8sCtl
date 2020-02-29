/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package kubie;

import java.util.Map;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public class K8sMetadata {

    public long creationTimestamp;
    protected final Map<String, Object> m;
    public String name;
    public String namespace;
    public Map<String, String> labels;

    public K8sMetadata(Object fromYaml) {
        if (fromYaml instanceof Map) {
            m = (Map<String, Object>) fromYaml;
            name = (String) m.remove("name");
            if (null == name) {
                name = "";
            }
            namespace = (String) m.remove("namespace");
            if (null == namespace) {
                namespace = "";
            }
            try {
                creationTimestamp = DateTime.parse((String) m.remove("creationTimestamp")).getMillis();
            } catch (Exception any) {
                creationTimestamp = System.currentTimeMillis();
            }
            labels = (Map<String, String>) m.remove("labels");
            return;
        }
        throw new RuntimeException("Got " + fromYaml.getClass().getSimpleName() + "=" + fromYaml + " where metadata was expected");
    }

}
