/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1DeploymentSpec;
import io.kubernetes.client.models.V1HostPathVolumeSource;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodTemplateSpec;
import io.kubernetes.client.models.V1Volume;
import io.kubernetes.client.models.V1VolumeMount;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author walter
 */
public class K8sApplication {

    private Detail getMount(String name) {
        if (null == details || details.isEmpty()) {
            return null;
        }
        for (Detail d : details) {
            if (d.isMount() && name.equals(d.name)) {
                return d;
            }
        }
        return null;
    }

    private boolean hasPorts() {
        if (null == details || details.isEmpty()) {
            return false;
        }
        for (Detail d : details) {
            if (d.isPort()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMounts() {
        if (null == details || details.isEmpty()) {
            return false;
        }
        for (Detail d : details) {
            if (d.isMount()) {
                return true;
            }
        }
        return false;
    }

    public static class Detail {

        public String type = "DirectoryOrCreate";
        public String name = "";
        public String source = "";
        public String target = "";

        public boolean isMount() {
            if (null == type || type.isEmpty()) {
                type = "DirectoryOrCreate";
                return true;
            }
            return "DirectoryOrCreate".equals(type);
        }

        public boolean isPort() {
            if (null == type || type.isEmpty()) {
                type = "DirectoryOrCreate";
                return false;
            }
            return !"DirectoryOrCreate".equals(type);
        }
    }
    public String appName;
    public LinkedList<String> args;
    public LinkedList<String> command;
    public String image;
    public String namespace;
    public LinkedList<Detail> details;

    public K8sApplication() {
        clear();
    }

    private void clear() {
        this.namespace = "";
        this.appName = "";
        this.image = "";
        this.args = new LinkedList<>();
        this.command = new LinkedList<>();
        this.details = new LinkedList<>();
    }

    public K8sApplication(K8sDeployment d) {
        clear();
        this.namespace = "" + d.getNamespace();
        this.appName = "" + d.getApp();
        V1DeploymentSpec spec = d.getK8s().getSpec();
        V1PodSpec pod = null;
        if (null != spec) {
            V1PodTemplateSpec template = spec.getTemplate();
            if (null != template) {
                pod = template.getSpec();
            }
        }
        copyFrom(pod);
    }

    public K8sApplication(K8sPod d) {
        clear();
        this.namespace = "" + d.getNamespace();
        this.appName = "" + d.getApp();
        V1PodSpec pod = d.getK8s().getSpec();
        copyFrom(pod);
    }

    public String toYaml(boolean asDeployment, boolean withNamespace) {
        StringBuilder yaml = new StringBuilder();
        if (!namespace.isEmpty() && withNamespace) {
            yaml.append("apiVersion: v1\n");
            yaml.append("kind: Namespace\n");
            yaml.append("metadata:\n");
            yaml.append("  name: ").append(namespace).append('\n');
            yaml.append("\n---\n\n");
        }
        yaml.append("apiVersion: ").append(asDeployment ? "apps/v1" : "v1").append('\n');
        yaml.append("kind: ").append(asDeployment ? "Deployment" : "Pod").append('\n');
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(appName).append('\n');
        if (!namespace.isEmpty()) {
            yaml.append("  namespace: ").append(namespace).append('\n');
        }
        if (asDeployment) {
            yaml.append("  labels:\n");
            yaml.append("    app: ").append(appName).append('\n');
        }
        yaml.append("spec:\n");
        if (!asDeployment) {
            yaml.append("  containers:\n");
            yaml.append("  - name: ").append(appName).append('\n');
            yaml.append("    image: ").append(image).append('\n');
            specDetails("", yaml);
        } else {
            yaml.append("  replicas: 1\n");
            yaml.append("  selector:\n");
            yaml.append("    matchLabels:\n");
            yaml.append("      app: ").append(appName).append('\n');
            yaml.append("  template:\n");
            yaml.append("    metadata:\n");
            yaml.append("      labels:\n");
            yaml.append("        app: ").append(appName).append('\n');
            yaml.append("    spec:\n");
            yaml.append("      containers:\n");
            yaml.append("      - name: ").append(appName).append('\n');
            yaml.append("        image: ").append(image).append('\n');
            specDetails("    ", yaml);
        }
        if (hasPorts()) {
            yaml.append("\n---\n\n");
            yaml.append("apiVersion: v1\n");
            yaml.append("kind: Service\n");
            yaml.append("metadata:\n");
            yaml.append("  name: ").append(appName).append('\n');
            if (!namespace.isEmpty()) {
                yaml.append("  namespace: ").append(namespace).append('\n');
            }
            yaml.append("spec:\n");
            yaml.append("  ports:\n");
            for (Detail p : details) {
                if (p.isPort()) {
                    yaml.append("  - name: ").append(p.name).append('\n');
                    yaml.append("    port: ").append(p.source).append('\n');
                    yaml.append("    targetPort: ").append(p.target).append('\n');
                    yaml.append("    protocol: ").append(p.type).append('\n');
                }
            }
            yaml.append("  selector:\n");
            yaml.append("    app: ").append(appName).append('\n');
            yaml.append("  type: ").append("NodePort").append('\n');
        }
        return yaml.toString();
    }

    private void specDetails(String prf, StringBuilder yaml) {
        if (!command.isEmpty()) {
            yaml.append(prf).append("    command:\n");
            for (String s : command) {
                yaml.append(prf).append("    - \"").append(s).append("\"\n");
            }
        }
        if (!args.isEmpty()) {
            yaml.append(prf).append("    args:\n");
            for (String s : args) {
                yaml.append(prf).append("    - \"").append(s).append("\"\n");
            }
        }
        if (hasPorts()) {
            yaml.append(prf).append("    ports:\n");
            for (Detail p : details) {
                if (p.isPort()) {
                    yaml.append(prf).append("    - name: ").append(p.name).append('\n');
                    yaml.append(prf).append("      containerPort: ").append(p.target).append('\n');
                    yaml.append(prf).append("      protocol: ").append(p.type).append('\n');
                    if (!p.source.trim().equals(p.target.trim())) {
                        yaml.append(prf).append("      hostPort: ").append(p.source).append('\n');
                    }
                }
            }
        }
        if (hasMounts()) {
            yaml.append(prf).append("    volumeMounts:\n");
            for (Detail m : details) {
                if (m.isMount()) {
                    yaml.append(prf).append("    - name: ").append(m.name).append('\n');
                    yaml.append(prf).append("      mountPath: ").append(m.target).append('\n');
                }
            }
        }
        yaml.append(prf).append("  restartPolicy: Always\n");
        if (hasMounts()) {
            yaml.append(prf).append("  volumes:\n");
            for (Detail m : details) {
                if (m.isMount()) {
                    yaml.append(prf).append("    - name: ").append(m.name).append('\n');
                    yaml.append(prf).append("      hostPath:\n");
                    yaml.append(prf).append("        path: ").append(m.source).append('\n');
                    yaml.append(prf).append("        type: DirectoryOrCreate\n");
                }
            }
        }
    }

    private void copyFrom(V1PodSpec pod) {
        if (null == pod) {
            return;
        }
        List<V1Container> containers = pod.getContainers();
        if (null == containers || containers.isEmpty()) {
            return;
        }
        V1Container cont = containers.get(0);
        if (null == cont) {
            return;
        }
        this.image = cont.getImage();
        if (null != cont.getArgs()) {
            this.args.addAll(cont.getArgs());
        }
        if (null != cont.getCommand()) {
            this.command.addAll(cont.getCommand());
        }
        List<V1ContainerPort> pl = cont.getPorts();
        if (null != pl) {
            for (V1ContainerPort port : pl) {
                Detail prt = new Detail();
                prt.type = port.getProtocol();
                prt.target = "" + port.getContainerPort();
                prt.name = port.getName();
                if (null != port.getHostPort()) {
                    prt.source = "" + port.getHostPort();
                } else {
                    prt.source = prt.target;
                }
                if (null == prt.name) {
                    prt.name = String.format("%s-%s", appName, prt.target);
                }
                details.add(prt);
            }
        }
        List<V1VolumeMount> volumeMounts = cont.getVolumeMounts();
        if (null != volumeMounts) {
            for (V1VolumeMount m : volumeMounts) {
                Detail mnt = getMount(m.getName());
                if (null == mnt) {
                    mnt = new Detail();
                }
                mnt.name = m.getName();
                mnt.target = m.getMountPath();
                details.add(mnt);
            }
        }
        List<V1Volume> volumes = pod.getVolumes();
        if (null != volumes) {
            for (V1Volume v : volumes) {
                Detail mnt = getMount(v.getName());
                if (null == mnt) {
                    mnt = new Detail();
                }
                mnt.name = v.getName();
                V1HostPathVolumeSource hostPath = v.getHostPath();
                mnt.source = (null == hostPath) ? "ext" : hostPath.getPath();
                details.add(mnt);
            }
        }
    }
}
