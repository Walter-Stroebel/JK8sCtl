/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package kubie;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author walter
 */
public abstract class MakeGraph implements Traverser {

    protected int seq = 0;
    protected final TreeMap<String, Integer> nodes = new TreeMap<>();
    protected final TreeMap<Integer, TreeSet<Integer>> edges = new TreeMap<>();

    public abstract boolean want(String key);
    public abstract int getEdge(String key);

    @Override
    public boolean leaf(K8sPath path, Object object) {
        String obj = "" + object;
        if (want(obj)) {
            Integer fromNS = nodes.get(path.getNamespace());
            if (null == fromNS) {
                nodes.put(path.getNamespace(), fromNS = ++seq);
            }
            String kinds = path.getNamespace() + " " + path.getKind();
            kinds = kinds.trim();
            Integer toKind = nodes.get(kinds);
            if (null == toKind) {
                nodes.put(kinds, toKind = ++seq);
                TreeSet<Integer> ts = edges.get(fromNS);
                if (null == ts) {
                    edges.put(fromNS, ts = new TreeSet<>());
                }
                ts.add(toKind);
            }            
            TreeSet<Integer> ts = edges.get(toKind);
            if (null == ts) {
                edges.put(toKind, ts = new TreeSet<>());
            }
            ts.add(getEdge(obj));
        }
        return true;
    }

    @Override
    public boolean path(K8sPath path) {
        return true;
    }

    public StringBuilder dot() {
        StringBuilder dot = new StringBuilder();
        dot.append("strict digraph {\nrankdir=\"LR\";\n");
        for (Map.Entry<String, Integer> e : nodes.entrySet()) {
            dot.append("nd").append(e.getValue()).append("[label=\"").append(e.getKey()).append("\"];\n");
        }
        for (Map.Entry<Integer, TreeSet<Integer>> e : edges.entrySet()) {
            for (Integer t : e.getValue()) {
                dot.append("nd").append(e.getKey()).append(" -> ").append("nd").append(t).append(";\n");
            }
        }
        dot.append("}");
        return dot;
    }
}
