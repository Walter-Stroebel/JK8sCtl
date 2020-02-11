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
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import java.util.List;
import java.util.Map;

/**
 *
 * @author walter
 */
public class Overview {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        {
            CoreV1Api api = new CoreV1Api();
            {
                V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
                for (V1Pod item : list.getItems()) {
                    new K8sPod(item);
                }
            }
            {
                V1NodeList list = api.listNode(null, null, null, null, null, null, null, null, null);
                for (V1Node item : list.getItems()) {
                    new K8sNode(item);
                }
            }
            {
                V1ServiceList list = api.listServiceForAllNamespaces(null, null, null, null, null, null, null, null, null);
                for (V1Service item : list.getItems()) {
                    new K8sService(item);
                }
            }
            {
                V1EndpointsList list = api.listEndpointsForAllNamespaces(null, null, null, null, null, null, null, null, null);
                for (V1Endpoints item : list.getItems()) {
                    new K8sEndpoints(item);
                }
            }
        }
        {
            AppsV1Api api = new AppsV1Api();
            {
                V1DeploymentList list = api.listDeploymentForAllNamespaces(null, null, null, null, null, null, null, null, null);
                for (V1Deployment e : list.getItems()) {

                }
            }
        }
        for (Map.Entry<String, List<Metadata>> e : Maps.byNAME.entrySet()) {
            System.out.println(e.getKey());
            for (Metadata m : e.getValue()){
                System.out.println("    "+m.getUUID()+" "+m.getKind());
            }
        }
    }

}
