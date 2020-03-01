/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package kubie;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.infcomtec.jk8sctl.Global;
import nl.infcomtec.jk8sctl.K8sCondition;
import nl.infcomtec.jk8sctl.K8sRelation;
import nl.infcomtec.jk8sctl.K8sStatus;
import nl.infcomtec.jk8sctl.Kinds;
import nl.infcomtec.jk8sctl.Metadata;
import org.joda.time.DateTime;

/**
 *
 * @author walter
 */
public class K8sObject implements Metadata {

    private static final AtomicInteger nextMapId = new AtomicInteger(0);
    String apiVersion;
    List<Object> items;
    protected final K8sMetadata k8sMd;
    private Kinds kind;
    protected Map<String, Object> map;
    final int mapId;
    Map<String, Object> spec;
    Map<String, Object> status;
    List<Object> subsets;

    public K8sObject(Object item) {
        this.mapId = nextMapId();
        map = (Map<String, Object>) item;
        k8sMd = new K8sMetadata(map.remove("metadata"));
        apiVersion = (String) map.remove("apiVersion");
        kind = Kinds.valueOf(((String) map.remove("kind")).toLowerCase());
        items = (List<Object>) map.remove("items");
        spec = (Map<String, Object>) map.remove("spec");
        status = (Map<String, Object>) map.remove("status");
        subsets = (List<Object>) map.remove("subsets");
    }

    private void addTree(DefaultMutableTreeNode parent, Map.Entry<String, Object> e) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(e.getKey());
        if (!addTree(node, e.getValue())) {
            node = new DefaultMutableTreeNode(e.getKey() + "=" + e.getValue());
        }
        parent.add(node);
    }

    private boolean addTree(DefaultMutableTreeNode parent, Object obj) {
        if (obj instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
                addTree(parent, e);
            }
            return true;
        } else if (obj instanceof List) {
            for (Object sub : (List) obj) {
                DefaultMutableTreeNode list = new DefaultMutableTreeNode();
                if (!addTree(list, sub)) {
                    list = new DefaultMutableTreeNode("" + sub);
                }
                parent.add(list);
            }
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Metadata o) {
        int c = getNamespace().compareTo(o.getNamespace());
        if (0 != c) {
            return c;
        }
        c = getKind().compareTo(o.getKind());
        if (0 != c) {
            return c;
        }
        return getName().compareTo(o.getName());
    }

    public String getApp() {
        Map<String, String> lbs = k8sMd.labels;
        if (null == lbs) {
            return null;
        }
        if (lbs.containsKey("app")) {
            return lbs.get("app");
        }
        if (lbs.containsKey("k8s-app")) {
            return lbs.get("k8s-app");
        }
        return null;
    }

    @Override
    public DateTime getCreationTimestamp() {
        return new DateTime(k8sMd.creationTimestamp);
    }

    @Override
    public StringBuilder getDotNode() {
        StringBuilder ret = pre();
        post(ret);
        return ret;
    }

    @Override
    public String getDotNodeName() {
        return "nd" + mapId;
    }

    @Override
    public Kinds getKind() {
        return kind;
    }

    @Override
    public int getMapId() {
        return (int) mapId;
    }

    @Override
    public String getNSName() {
        if (!k8sMd.namespace.isEmpty()) {
            return k8sMd.namespace + "." + k8sMd.name;
        }
        return k8sMd.name;
    }

    @Override
    public String getName() {
        return k8sMd.name;
    }

    @Override
    public String getNamespace() {
        return k8sMd.namespace;
    }

    public K8sObject onNode() {
        if (getKind() == Kinds.pod) {
            try {
                return Maps.get(Kinds.node, spec.get("nodeName").toString());
            } catch (Exception any) {
                Global.warn("Pod %s does not have a node?", getNSName());
            }
        }
        return null;
    }

    @Override
    public TreeMap<Integer, K8sRelation> getRelations() {
        TreeMap<Integer, K8sRelation> ret = new TreeMap<>();
        {
            K8sObject ns = Maps.get(Kinds.namespace, getNamespace());
            if (null != ns) {
                ret.put(ns.getMapId(), new K8sRelation(false, ns.getMapId(), "ns"));
            }
        }
        String app = getApp();
        if (null != app) {
            for (Metadata md : Maps.getAll(null)) {
                if (!md.getKind().equals(getKind()) && md.getNamespace().equals(getNamespace()) && md.getName().equals(app)) {
                    ret.put(md.getMapId(), new K8sRelation(true, md.getMapId(), md.getKind().name()));
                }
            }
        }
        K8sObject node = onNode();
        if (null != node) {
            ret.put(node.getMapId(), new K8sRelation(false, node.getMapId(), node.getKind().name()));
        }
        return ret;
    }

    @Override
    public K8sStatus getStatus() {
        switch (getKind()) {
            case deployment: {
                if (null == status) {
                    return new K8sStatus(false);
                }
                int actRep = null == status.get("replicas") ? 0 : (int) status.get("replicas");
                K8sStatus ret = new K8sStatus();
                List<Map<String, Object>> conditions = (List<Map<String, Object>>) status.get("conditions");
                for (Map<String, Object> cond : conditions) {
                    String t = (String) cond.get("type");
                    String s = (String) cond.get("status");
                    K8sCondition kc = new K8sCondition(cond);
                    if (s.equals("Unknown")) {
                        ret.okay = false;
                        kc.isAnIssue = K8sCondition.Status.True;
                    } else if (t.equals("Progressing") && !s.equals("True")) {
                        ret.okay = false;
                        kc.isAnIssue = K8sCondition.Status.True;
                    } else if (t.equals("Progressing") && s.equals("True")) {
                        kc.isAnIssue = K8sCondition.Status.False;
                    } else if (t.endsWith("Available") && !s.equals("True")) {
                        ret.okay = false;
                        kc.isAnIssue = K8sCondition.Status.True;
                    } else if (t.endsWith("Available") && s.equals("True")) {
                        kc.isAnIssue = K8sCondition.Status.False;
                    }
                    ret.details.put(t, kc);
                }
                if (null == spec) {
                    ret.okay = false;
                } else {
                    int desRep = null == spec.get("replicas") ? 0 : (int) spec.get("replicas");
                    if (actRep != desRep) {
                        ret.okay = false;
                        ret.details.put("Replicas", new K8sCondition(String.format("Replicas %d of %d", actRep, desRep), K8sCondition.Status.False, K8sCondition.Status.True));
                    } else {
                        ret.details.put("Replicas", new K8sCondition(String.format("Replicas %d of %d", actRep, desRep), K8sCondition.Status.True, K8sCondition.Status.False));
                    }
                }
                return ret;
            }
            default:
                // no news is good news
                return new K8sStatus(true);
        }
    }

    @Override
    public DefaultMutableTreeNode getTree() {
        DefaultMutableTreeNode ret;
        if (getNamespace().isEmpty()) {
            ret = new DefaultMutableTreeNode(getKind() + "=" + getName());
        } else {
            ret = new DefaultMutableTreeNode(getNSName() + ((null == getApp() || getApp().isEmpty()) ? "" : " " + getApp()));
        }
        if (null != apiVersion) {
            ret.add(new DefaultMutableTreeNode("apiVersion=" + apiVersion));
        }
        ret.add(new DefaultMutableTreeNode("kind=" + getKind()));
        ret.add(new DefaultMutableTreeNode("creationTimestamp=" + getCreationTimestamp()));
        if (null != getName()) {
            ret.add(new DefaultMutableTreeNode("name=" + getName()));
        }
        if (null != k8sMd.labels) {
            for (Map.Entry<String, String> e : k8sMd.labels.entrySet()) {
                ret.add(new DefaultMutableTreeNode(e.getKey() + "=" + e.getValue()));
            }
        }
        if (null != spec) {
            DefaultMutableTreeNode nd = new DefaultMutableTreeNode("spec");
            for (Map.Entry<String, Object> e : spec.entrySet()) {
                addTree(nd, e);
            }
            ret.add(nd);
        }
        if (null != status) {
            DefaultMutableTreeNode nd = new DefaultMutableTreeNode("status");
            for (Map.Entry<String, Object> e : status.entrySet()) {
                addTree(nd, e);
            }
            ret.add(nd);
        }
        if (null != subsets) {
            DefaultMutableTreeNode nd = new DefaultMutableTreeNode("subsets");
            for (Object o : subsets) {
                addTree(nd, o);
            }
            ret.add(nd);
        }
        DefaultMutableTreeNode extra = new DefaultMutableTreeNode("extra");
        for (Map.Entry<String, Object> e : k8sMd.m.entrySet()) {
            addTree(extra, e);
        }
        if (!extra.isLeaf()) {
            ret.add(extra);
        }
        if (null != items) {
            for (Object obj : items) {
                if (obj instanceof Map) {
                    for (Map.Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
                        extra.add(new DefaultMutableTreeNode("metadata." + e.getKey() + "=" + e.getValue()));
                    }
                } else {
                    extra.add(new DefaultMutableTreeNode(obj.getClass().getSimpleName() + "=" + obj));
                }
            }
        }
        if (!map.isEmpty()) {
            DefaultMutableTreeNode unMapped = new DefaultMutableTreeNode("unmapped");
            for (Map.Entry<String, Object> e : map.entrySet()) {
                addTree(unMapped, e);
            }
            ret.add(unMapped);
        }
        if (getKind() == Kinds.namespace) {
            TreeMap<Kinds, DefaultMutableTreeNode> byKind = new TreeMap<>();
            for (K8sObject e : Maps.getByNS(getName())) {
                DefaultMutableTreeNode sub = byKind.get(e.getKind());
                if (null == sub) {
                    byKind.put(e.getKind(), sub = new DefaultMutableTreeNode(e.getKind()));
                }
                sub.add(e.getTree());
            }
            for (Map.Entry<Kinds, DefaultMutableTreeNode> e : byKind.entrySet()) {
                ret.add(e.getValue());
            }
        }
        return ret;
    }

    public int nextMapId() {
        return nextMapId.incrementAndGet();
    }

    protected void post(StringBuilder sb) {
        sb.append("</TABLE>>];\n");
    }

    protected StringBuilder pre() {
        StringBuilder ret = new StringBuilder();
        ret.append(getDotNodeName()).append("[label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">\n");
        ret.append("<TR><TD>").append(getKind()).append("</TD><TD>").append(getName()).append("</TD></TR>");
        return ret;
    }

    private FoundLeaf trList(K8sPath path, List<Object> list, Traverser tr) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        if (tr.path(path)) {
            for (Object obj : list) {
                if (null != obj) {
                    if (obj instanceof K8sObject) {
                        FoundLeaf fl = ((K8sObject) obj).traverse(path, tr);
                        if (null != fl) {
                            return fl;
                        }
                    } else if (obj instanceof List) {
                        FoundLeaf fl = trList(path, (List) obj, tr);
                        if (null != fl) {
                            return fl;
                        }
                    } else if (obj instanceof Map) {
                        FoundLeaf fl = trMap(path, (Map<String, Object>) obj, tr);
                        if (null != fl) {
                            return fl;
                        }
                    } else if (!tr.leaf(path, obj)) {
                        return new FoundLeaf(path, obj);
                    }
                }
            }
        }
        return null;
    }

    private FoundLeaf trMap(K8sPath _path, Map<String, Object> map, Traverser tr) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        if (tr.path(_path)) {
            for (Map.Entry<String, Object> e : map.entrySet()) {
                K8sPath path = _path.append(e.getKey());
                if (null != e.getValue() && tr.path(path)) {
                    Object obj = e.getValue();
                    if (obj instanceof K8sObject) {
                        FoundLeaf fl = ((K8sObject) obj).traverse(path, tr);
                        if (null != fl) {
                            return fl;
                        }
                    } else if (obj instanceof List) {
                        FoundLeaf fl = trList(path, (List) obj, tr);
                        if (null != fl) {
                            return fl;
                        }
                    } else if (obj instanceof Map) {
                        FoundLeaf fl = trMap(path, (Map<String, Object>) obj, tr);
                        if (null != fl) {
                            return fl;
                        }
                    } else if (!tr.leaf(path, obj)) {
                        return new FoundLeaf(path, obj);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Traverse the entire Kubernetes object tree.
     *
     * @param tr Called for each path and leaf found.
     * @return Last object we called leaf() for which halted the traversal. null
     * if no leaves halted the traversal.
     */
    public FoundLeaf traverse(Traverser tr) {
        return traverse(new K8sPath(getNamespace(), getKind().name(), getName()), tr);
    }

    private FoundLeaf traverse(K8sPath _path, Traverser tr) {
        if (!tr.path(_path)) {
            return null;
        }
        K8sPath path = _path.append("apiVersion");
        if (!tr.leaf(path, apiVersion)) {
            return new FoundLeaf(path, apiVersion);
        }
        path = path.replaceLast("mapId");
        if (!tr.leaf(path, mapId)) {
            return new FoundLeaf(path, mapId);
        }
        path = path.replaceLast("creationTimestamp");
        if (!tr.leaf(path, k8sMd.creationTimestamp)) {
            return new FoundLeaf(path, k8sMd.creationTimestamp);
        }
        path = path.removeLast();
        if (null != k8sMd.labels) {
            path = path.append("labels");
            if (tr.path(path)) {
                for (Map.Entry<String, String> e : k8sMd.labels.entrySet()) {
                    path = path.append(e.getKey());
                    if (!tr.leaf(path, e.getValue())) {
                        return new FoundLeaf(path, e.getValue());
                    }
                    path = path.removeLast();
                }
            }
            path = path.removeLast();
        }
        {
            path = path.append("items");
            FoundLeaf fl = trList(path, items, tr);
            if (null != fl) {
                return fl;
            }
            path = path.removeLast();
        }
        {
            path = path.append("spec");
            FoundLeaf fl = trMap(path, spec, tr);
            if (null != fl) {
                return fl;
            }
            path = path.removeLast();
        }
        {
            path = path.append("spec");
            FoundLeaf fl = trMap(path, spec, tr);
            if (null != fl) {
                return fl;
            }
            path = path.removeLast();
        }
        {
            path = path.append("status");
            FoundLeaf fl = trMap(path, status, tr);
            if (null != fl) {
                return fl;
            }
            path = path.removeLast();
        }
        {
            path = path.append("subsets");
            FoundLeaf fl = trList(path, subsets, tr);
            if (null != fl) {
                return fl;
            }
            path = path.removeLast();
        }
        {
            path = path.append("unmapped");
            FoundLeaf fl = trMap(path, map, tr);
            if (null != fl) {
                return fl;
            }
            path.removeLast();
        }
        return null;
    }

    public static class FoundLeaf {

        public final K8sPath path;
        public final Object value;

        public FoundLeaf(K8sPath path, Object value) {
            this.path = path;
            this.value = value;
        }

        @Override
        public String toString() {
            return "FoundLeaf{" + "path=" + path + ", last=" + value + '}';
        }
    }
}
