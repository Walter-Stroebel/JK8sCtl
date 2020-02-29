/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.basicutils;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple path to an object.
 *
 * @author walter
 * @param <K> Type of each path element, quite likely to be String.
 */
public class ObjPath<K extends Comparable> implements Comparable<ObjPath> {

    private static AtomicLong seq = new AtomicLong(0);
    public final LinkedList<K> path;
    public final long uid;

    public ObjPath() {
        uid = seq.incrementAndGet();
        this.path = new LinkedList<>();
    }

    public ObjPath(LinkedList<K> path) {
        this();
        this.path.addAll(path);
    }

    public ObjPath(LinkedList<K> path, K part) {
        this(path);
        this.path.add(part);
    }

    public ObjPath(ObjPath<K> parent, K part) {
        this(parent.path);
        this.path.add(part);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (int) (this.uid ^ (this.uid >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ObjPath other = (ObjPath) obj;
        return this.uid == other.uid;
    }

    @Override
    public int compareTo(ObjPath o) {
        if (uid == o.uid) {
            return 0; // same object
        }
        int c = 0;
        for (int i = 0; c == 0; i++) {
            if (i == path.size()) {
                if (i == o.path.size()) {
                    return 0;
                } else {
                    return -1; // this path is shorter then the other path
                }
            } else if (i == o.path.size()) {
                return 1; // this path is longer then that other path
            } else {
                c = path.get(i).compareTo(o.path.get(i));
            }
        }
        return c;
    }

    @Override
    public String toString() {
        return toString('/');
    }

    public K peek() {
        return path.getLast();
    }

    private String toString(char sep) {
        StringBuilder ret = new StringBuilder();
        boolean notFirst = false;
        for (K e : path) {
            if (notFirst) {
                ret.append(sep);
            }
            notFirst = true;
            ret.append(e);
        }
        return ret.toString();
    }

}
