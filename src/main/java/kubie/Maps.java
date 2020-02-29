/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package kubie;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.infcomtec.jk8sctl.Global;
import nl.infcomtec.jk8sctl.Metadata;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author walter
 */
public class Maps {

    private static final Map<Integer, K8sObject> items = new HashMap<>();
    private static final Comparator<Metadata> mdComp = new Comparator<Metadata>() {

        @Override
        public int compare(Metadata o1, Metadata o2) {
            return o1.compareTo(o2);
        }

    };

    private static DefaultMutableTreeNode getRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (K8sObject e : getByNS("")) {
            root.add(e.getTree());
        }
        return root;
    }

    /**
     * Traverse the entire Kubernetes object tree.
     *
     * @param tr Called for each path and leaf found.
     * @return Last object we called leaf() for which halted the traversal. null
     * if no leaves halted the traversal.
     */
    public static K8sObject.FoundLeaf traverse(Traverser tr) {
        for (K8sObject e : getAll(null)) {
            K8sObject.FoundLeaf traverse = e.traverse(tr);
            if (null != traverse) {
                return traverse;
            }
        }
        return null;
    }

    public static LinkedList<K8sObject> getAll(String forKind) {
        LinkedList<K8sObject> ret = new LinkedList<>();
        synchronized (items) {
            if (null == forKind) {
                ret.addAll(items.values());
            } else {
                for (K8sObject obj : items.values()) {
                    if (obj.getKind().equals(forKind)) {
                        ret.add(obj);
                    }
                }
            }
        }
        ret.sort(mdComp);
        return ret;
    }

    public static LinkedList<K8sObject> getByNS(String namespace) {
        LinkedList<K8sObject> ret = new LinkedList<>();
        synchronized (items) {
            if (null == namespace) {
                namespace = "";
            }
            for (K8sObject obj : items.values()) {
                if (obj.getNamespace().equals(namespace)) {
                    ret.add(obj);
                }
            }
        }
        ret.sort(mdComp);
        return ret;
    }

    public static K8sObject get(String forKind, String name) {
        LinkedList<K8sObject> sel = getAll(forKind);
        for (K8sObject obj : sel) {
            if (obj.getNSName().equals(name)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();
        for (String what : new String[]{"namespaces", "nodes"}) {
            ProcessBuilder pb = new ProcessBuilder("kubectl", "get", what, "-o", "yaml");
            pb.inheritIO();
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process p = pb.start();
            Object load = yaml.load(p.getInputStream());
            if (0 != p.waitFor()) {
                Global.warn("Calling kubectl failed; is the cluster accessible?");
                System.exit(1);
            }
            copy(load);
        }
        for (String what : new String[]{"endpoints", "deployments", "replicationcontrollers", "pods", "services", "secrets", "limits"}) {
            ProcessBuilder pb = new ProcessBuilder("kubectl", "get", what, "-A", "-o", "yaml");
            pb.inheritIO();
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process p = pb.start();
            Object load = yaml.load(p.getInputStream());
            System.out.println(p.waitFor());
            copy(load);
        }
        MakeGraph mg = new MakeGraph() {
            @Override
            public int getEdge(String key) {
                String addr;
                try {
                    addr = InetAddress.getByName(key).getHostAddress();
                } catch (UnknownHostException ex) {
                    addr = "unknown";
                }
                StringTokenizer toker = new StringTokenizer(addr, ".:%/");
                int par = 0;
                StringBuilder part = new StringBuilder();
                while (toker.hasMoreTokens()) {
                    if (part.length() == 0) {
                        // topmost
                        part.append(toker.nextToken());
                        Integer top = nodes.get(part.toString());
                        if (null == top) {
                            nodes.put(part.toString(), top = ++seq);
                        }
                        par = top;
                    } else {
                        part.append('.');
                        part.append(toker.nextToken());
                        Integer nxt = nodes.get(part.toString());
                        if (null == nxt) {
                            nodes.put(part.toString(), nxt = ++seq);
                            TreeSet<Integer> right = new TreeSet<>();
                            right.add(par);
                            edges.put(nxt, right);
                        }
                        par = nxt;
                    }
                }
                return par;
            }

            @Override
            public boolean want(String key) {
                return isAddress(key);
            }
        };
        K8sObject.FoundLeaf traverse = traverse(mg);
        System.out.println(mg.dot());
        System.out.println(traverse);
        JFrame jf = new JFrame("Dump");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane pane = new JScrollPane(new JTree(getRoot()));
        jf.getContentPane().add(pane);
        jf.pack();
        jf.setVisible(true);
    }

    public static boolean isAddress(Object object) {
        if (object instanceof InetAddress) {
            return true;
        }
        if (object instanceof String) {
            String s = (String) object;
            if (!s.isEmpty() && Character.isDigit(s.charAt(0)) && s.indexOf('.') != s.lastIndexOf('.')) {
                try {
                    InetAddress.getByName(s);
                    return true;
                } catch (Exception ex) {
                }
            }
        }
        return false;
    }

    private static void copy(Object load) {
        K8sObject k8s = new K8sObject(load);
        if (null != k8s.items) {
            for (Object e : k8s.items) {
                K8sObject d = new K8sObject(e);
                items.put(d.getMapId(), d);
            }
        }
    }

}
