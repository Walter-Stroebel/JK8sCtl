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
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
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

    private static void add(Metadata md) {
        items.put(md.getMapId(), md);
        if (md instanceof K8sNamespace) {
            spaces.put(md.getName(), md.getMapId());
        }
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
            {
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
            }
            {
                CoreV1Api api = new CoreV1Api();
                {
                    V1NamespaceList list = api.listNamespace(null, null, null, null, null, null, null, null, null);
                    for (V1Namespace item : list.getItems()) {
                        add(new K8sNamespace(items.size(), item));
                    }
                }
                {
                    V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Pod item : list.getItems()) {
                        add(new K8sPod(items.size(), item));
                    }
                }
                {
                    V1NodeList list = api.listNode(null, null, null, null, null, null, null, null, null);
                    for (V1Node item : list.getItems()) {
                        add(new K8sNode(items.size(), item));
                    }
                }
                {
                    V1ServiceList list = api.listServiceForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Service item : list.getItems()) {
                        add(new K8sService(items.size(), item));
                    }
                }
                {
                    V1EndpointsList list = api.listEndpointsForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Endpoints item : list.getItems()) {
                        add(new K8sEndpoints(items.size(), item));
                    }
                }
            }
            AppsV1Api api = new AppsV1Api();
            {
                V1DeploymentList list = api.listDeploymentForAllNamespaces(null, null, null, null, null, null, null, null, null);
                for (V1Deployment item : list.getItems()) {
                    add(new K8sDeployment(items.size(), item));
                }
            }
        }
    }
}
