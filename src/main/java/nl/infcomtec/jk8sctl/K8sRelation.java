/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

/**
 *
 * @author walter
 */
public class K8sRelation {

    public final boolean isTo;
    public final int other;
    public final String label;

    public K8sRelation(boolean isTo, int other, String label) {
        this.isTo = isTo;
        this.other = other;
        this.label = label;
    }

    @Override
    public String toString() {
        if (isTo) {
            if (label.trim().isEmpty()) {
                return String.format(";\n");
            } else {
                return String.format(" [label=\"%s\"];\n", label);
            }
        } else {
            if (label.trim().isEmpty()) {
                return String.format(" [dir=back];\n");
            } else {
                return String.format(" [label=\"%s\" dir=back];\n", label);
            }
        }
    }

}
