/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package kubie;

import nl.infcomtec.basicutils.ObjPath;

/**
 * Path within Kubernetes.
 *
 * @author walter
 */
public class K8sPath extends ObjPath<String> {

    public K8sPath(String namespace, String kind, String name) {
        path.add(namespace);
        path.add(kind);
        path.add(name);
    }

    public K8sPath(K8sPath path) {
        super(path.path);
    }

    public K8sPath(K8sPath path, String part) {
        super(path.path, part);
    }

    public String getNamespace() {
        return path.getFirst();
    }

    public String getKind() {
        return path.get(1);
    }

    public String getName() {
        return path.get(2);
    }

    public String getFullName() {
        return getFullName('/');
    }

    protected K8sPath append(String part) {
        return new K8sPath(this, part);
    }

    protected K8sPath replaceLast(String part) {
        K8sPath ret = removeLast();
        ret.path.add(part);
        return ret;
    }

    protected K8sPath removeLast() {
        K8sPath ret= new K8sPath(this);
        ret.path.removeLast();
        return ret;
    }

    public String getFullName(char sep) {
        StringBuilder ret = new StringBuilder(getNamespace());
        return ret.append(sep).append(getKind()).append(sep).append(getName()).toString();
    }

    public String getRelativeName(char sep) {
        if (path.size() < 4) {
            return "";
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 3; i < path.size(); i++) {
            if (i > 3) {
                ret.append(sep);
            }
            ret.append(path.get(i));
        }
        return ret.toString();
    }

    public String getRelativeName() {
        return getRelativeName('/');
    }
}
