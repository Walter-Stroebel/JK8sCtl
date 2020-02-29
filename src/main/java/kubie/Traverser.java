/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package kubie;

/**
 * Passed as call-back to Traverse().
 *
 * @author walter
 */
public interface Traverser {

    /**
     * Found a leaf.
     *
     * @param path Path to leaf.
     * @param object Leaf object.
     * @return false to halt the traversal (eg. object found), true to continue
     */
    boolean leaf(K8sPath path, Object object);

    /**
     * Found a sub-path.
     *
     * @param path Branch we are about to traverse.
     * @return false to skip this branch, true to continue
     */
    boolean path(K8sPath path);
}
