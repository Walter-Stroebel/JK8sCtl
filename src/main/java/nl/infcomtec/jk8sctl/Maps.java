/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentList;
import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1EndpointsList;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1ReplicationController;
import io.kubernetes.client.models.V1ReplicationControllerList;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public class Maps {

    public static final ConcurrentSkipListMap<Integer, Metadata> items = new ConcurrentSkipListMap<>();
    public static final ConcurrentSkipListMap<String, Integer> spaces = new ConcurrentSkipListMap<>();
    public static final ConcurrentSkipListMap<String, TreeSet<Integer>> apps = new ConcurrentSkipListMap<>();

    private static void add(Metadata md) {
        items.put(md.getMapId(), md);
        if (md instanceof K8sNamespace) {
            spaces.put(md.getName(), md.getMapId());
        } else if (md instanceof AbstractAppReference) {
            AbstractAppReference ref = (AbstractAppReference) md;
            if (null != ref.getApp()) {
                String key = ref.getNamespace() + "." + ref.getApp();
                TreeSet<Integer> set = apps.get(key);
                if (null == set) {
                    apps.put(key, set = new TreeSet<>());
                }
                set.add(md.getMapId());
            }
        }
    }

    public static String[] getSpaces() {
        String[] ret = new String[spaces.size()];
        int i = 0;
        for (String k : spaces.keySet()) {
            ret[i++] = k;
        }
        return ret;
    }

    /**
     * Collect all information from the connected Kubernetes cluster.
     *
     * @throws java.lang.Exception
     */
    public static void collect() throws Exception {
        ApiClient client = Global.getK8sClient();
        Configuration.setDefaultApiClient(client);
        synchronized (items) {
            items.clear();
            spaces.clear();
            apps.clear();
            try {
                V1Namespace root = new V1Namespace();
                V1ObjectMeta meta = new V1ObjectMeta();
                meta.setName("(root)");
                meta.setCreationTimestamp(new DateTime());
                meta.setUid(UUID.randomUUID().toString());
                meta.setNamespace("");
                root.setApiVersion("n/a");
                root.setKind("namespace");
                root.setMetadata(meta);
                add(new K8sNamespace(0, root));
                spaces.put("", 0);
            } catch (Exception any) {
                System.out.println("AddRoot: " + any);
            }
            {
                CoreV1Api api = new CoreV1Api();
                try {
                    V1NamespaceList list = api.listNamespace(null, null, null, null, null, null, null, null, null);
                    for (V1Namespace item : list.getItems()) {
                        add(new K8sNamespace(items.size(), item));
                    }
                } catch (Exception any) {
                    System.out.println("listNamespace: " + any);
                }
                try {
                    V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Pod item : list.getItems()) {
                        add(new K8sPod(items.size(), item));
                    }
                } catch (Exception any) {
                    System.out.println("listPodForAllNamespaces: " + any);
                }
                try {
                    V1NodeList list = api.listNode(null, null, null, null, null, null, null, null, null);
                    for (V1Node item : list.getItems()) {
                        add(new K8sNode(items.size(), item));
                    }
                } catch (Exception any) {
                    System.out.println("listNode: " + any);
                }
                try {
                    V1ServiceList list = api.listServiceForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Service item : list.getItems()) {
                        add(new K8sService(items.size(), item));
                    }
                } catch (Exception any) {
                    System.out.println("listServiceForAllNamespaces: " + any);
                }
                try {
                    V1EndpointsList list = api.listEndpointsForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Endpoints item : list.getItems()) {
                        add(new K8sEndpoints(items.size(), item));
                    }
                } catch (Exception any) {
                    System.out.println("listEndpointsForAllNamespaces: " + any);
                }
                try {
                    V1ReplicationControllerList list = api.listReplicationControllerForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1ReplicationController item : list.getItems()) {
                        add(new K8sReplicationController(items.size(), item));
                    }
                } catch (Exception any) {
                    System.out.println("listReplicationControllerForAllNamespaces: " + any);
                }
            }
            try {
                AppsV1Api api = new AppsV1Api();
                {
                    V1DeploymentList list = api.listDeploymentForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Deployment item : list.getItems()) {
                        add(new K8sDeployment(items.size(), item));
                    }
                }
            } catch (Exception any) {
                System.out.println("listDeploymentForAllNamespaces: " + any);
            }
        }
    }
}
