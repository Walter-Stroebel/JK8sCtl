/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1Endpoints;
import io.kubernetes.client.openapi.models.V1EndpointsList;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1ReplicationController;
import io.kubernetes.client.openapi.models.V1ReplicationControllerList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public class Maps {

    public static final ConcurrentSkipListMap<String, TreeSet<Integer>> apps = new ConcurrentSkipListMap<>();
    public static final ConcurrentSkipListMap<Integer, Metadata> items = new ConcurrentSkipListMap<>();
    private static final ConcurrentLinkedDeque<CollectorUpdate> needUpdate = new ConcurrentLinkedDeque<>();
    public static final ConcurrentSkipListMap<String, K8sResources> nodes = new ConcurrentSkipListMap<>();
    public static final ConcurrentSkipListMap<String, PodDockerStats> podDockerStats = new ConcurrentSkipListMap<>();
    public static final ConcurrentSkipListMap<String, Integer> spaces = new ConcurrentSkipListMap<>();

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
            nodes.clear();
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
                    V1NamespaceList list = api..listNamespace();
                    for (V1Namespace item : list.getItems()) {
                        add(new K8sNamespace(items.size(), item));
                    }
                } catch (Exception any) {
                    System.out.println("listNamespace: " + any);
                }
                try {
                    V1NodeList list = api.listNode(null, null, null, null, null, null, null, null, null);
                    for (V1Node item : list.getItems()) {
                        K8sNode n = new K8sNode(items.size(), item);
                        add(n);
                        nodes.put(n.getName(), n.getResources());
                    }
                } catch (Exception any) {
                    System.out.println("listNode: " + any);
                }
                try {
                    V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
                    for (V1Pod item : list.getItems()) {
                        K8sPod p = new K8sPod(items.size(), item);
                        add(p);
                        K8sResources res = nodes.get(p.getNodeName());
                        if (null == res) {
                            nodes.put(p.getNodeName(), res = new K8sResources());
                            Global.warn("Pod %s claims to run on unknown node %s", p.getNSName(), p.getNodeName());
                        }
                        res.podUsed++;
                        double c = 0;
                        double d = 0;
                        double m = 0;
                        for (V1Container e1 : p.getK8s().getSpec().getContainers()) {
                            {
                                Map<String, Quantity> mp = e1.getResources().getRequests();
                                if (null != mp) {
                                    for (Map.Entry<String, Quantity> e2 : mp.entrySet()) {
                                        switch (e2.getKey()) {
                                            case "cpu":
                                                c = e2.getValue().getNumber().doubleValue();
                                                break;
                                            case "ephemeral-storage":
                                                d = e2.getValue().getNumber().doubleValue();
                                                break;
                                            case "memory":
                                                m = e2.getValue().getNumber().doubleValue();
                                                break;
                                        }
                                    }
                                }
                            }
                            {
                                Map<String, Quantity> mp = e1.getResources().getLimits();
                                if (null != mp) {
                                    for (Map.Entry<String, Quantity> e2 : mp.entrySet()) {
                                        switch (e2.getKey()) {
                                            case "cpu":
                                                c = Math.max(c, e2.getValue().getNumber().doubleValue());
                                                break;
                                            case "ephemeral-storage":
                                                d = Math.max(d, e2.getValue().getNumber().doubleValue());
                                                break;
                                            case "memory":
                                                m = Math.max(m, e2.getValue().getNumber().doubleValue());
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                        res.cpuUsed += c;
                        res.memUsed += m;
                        res.dskUsed += d;
                    }
                } catch (Exception any) {
                    System.out.println("listPodForAllNamespaces: " + any);
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
        for (Iterator<CollectorUpdate> it = needUpdate.iterator(); it.hasNext();) {
            CollectorUpdate u = it.next();
            if (!u.update()) {
                it.remove();
            }
        }
    }

    public static void doUpdate(CollectorUpdate f) {
        needUpdate.add(f);
    }

    public static void getNodeDockerStats(K8sNode node) throws Exception {
        K8sCtlCfg config = Global.getConfig();
        K8sResources resources = node.getResources();
        // need to figure this out
        String hostFile = config.getModString("nodessh.hostFile", new File(Global.workDir, "known_nodes").getAbsolutePath(), true);
        String nodeUser = config.getModString("nodessh.nodeUser", "root", true);
        String publicKF = config.getModString("nodessh.publicKeyFile", new File(new File(Global.homeDir, ".ssh"), "id_rsa").getAbsolutePath(), true);
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(node.getName());
        ssh.authPublickey(nodeUser, publicKF);
        try (Session session = ssh.startSession()) {
            Session.Command cmd = session.exec("docker stats --format \"{{.ID}} {{.Name}} {{.CPUPerc}} {{.MemUsage}}\" --no-stream");
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                    PodDockerStats stats = new PodDockerStats(node.getName(), s,resources);
                    podDockerStats.put(stats.dockerId, stats);
                }
            }
            cmd.join();
        }
        ssh.disconnect();
    }

    public static String[] getSpaces() {
        String[] ret = new String[spaces.size()];
        int i = 0;
        for (String k : spaces.keySet()) {
            ret[i++] = k;
        }
        return ret;
    }

    public static String analyse() throws Exception {
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                pw.println();
                pw.format("%-20s", "Kubernetes Node");
                pw.format("%-55s", "Namespace and pod name");
                pw.format("%6s %8s %8s ", "CPU%", "Mem MiB", "Mem Max");
                pw.format("%6s %8s %8s\n", "PodCPU", "PodMem", "PodMax");
                podDockerStats.clear();
                for (Metadata item : items.values()) {
                    if (item instanceof K8sNode) {
                        getNodeDockerStats((K8sNode) item);
                    }
                }
                for (PodDockerStats e : podDockerStats.values()) {
                    String docId = "docker://" + e.dockerId;
                    for (Metadata item : items.values()) {
                        if (item instanceof K8sPod) {
                            K8sPod pod = (K8sPod) item;
                            if (!pod.getNodeName().equalsIgnoreCase(e.node)) {
                                continue;
                            }
                            V1PodStatus status = pod.getK8s().getStatus();
                            List<V1ContainerStatus> containerStatuses = status.getContainerStatuses();
                            for (V1ContainerStatus cs : containerStatuses) {
                                if (cs.getContainerID().startsWith(docId)) {
                                    e.pod = pod;
                                    break;
                                } else if (e.dockName.contains(item.getName())) {
                                    e.pod = pod;
                                }
                            }
                            if (null != e.pod) {
                                break;
                            }
                        }
                    }
                }
                for (PodDockerStats e : podDockerStats.values()) {
                    pw.format("%-20s", e.node);
                    pw.format("%-55s", null == e.pod ? e.dockerId : e.pod.getNSName());
                    pw.format("%6.2f %8.2f %8.2f ", e.cpuPer, e.memUse / Global.MiBf, e.memMax / Global.MiBf);
                    if (null!=e.pod){
                        K8sResources rs = e.pod.getResources();
                        pw.format("%6.2f %8.2f %8.2f\n", rs.cpuUsed*100.0/e.resources.cpuAvail, rs.memUsed / Global.MiBf, rs.memAvail / Global.MiBf);
                    }else{
                        pw.println();
                    }
                }
            }
            sw.flush();
            return sw.toString();
        }
    }

    public static class PodDockerStats {

        public final String dockName;

        public final String node;
        public final String dockerId;
        public final double cpuPer;
        public final long memUse;
        public final long memMax;
        public K8sPod pod = null;
        public final K8sResources resources;

        public PodDockerStats(String node, String s,K8sResources resources) {
            this.node = node;
            this.resources=resources;
            StringTokenizer toker = new StringTokenizer(s, " :/%");
            dockerId = toker.nextToken();
            dockName = toker.nextToken();
            cpuPer = Double.parseDouble(toker.nextToken());
            memUse = makeBytes(toker.nextToken());
            memMax = makeBytes(toker.nextToken());
        }

        private static long makeBytes(String s) {
            if (s.endsWith("KiB")) {
                return Math.round(Double.parseDouble(s.substring(0, s.length() - 3)) * 1024.0);
            } else if (s.endsWith("MiB")) {
                return Math.round(Double.parseDouble(s.substring(0, s.length() - 3)) * 1024.0 * 1024.0);
            } else if (s.endsWith("GiB")) {
                return Math.round(Double.parseDouble(s.substring(0, s.length() - 3)) * 1024.0 * 1024.0 * 1024.0);
            } else if (s.endsWith("TiB")) {
                return Math.round(Double.parseDouble(s.substring(0, s.length() - 3)) * 1024.0 * 1024.0 * 1024.0 * 1024.0);
            } else {
                return Math.round(Double.parseDouble(s));
            }
        }

    }
}
