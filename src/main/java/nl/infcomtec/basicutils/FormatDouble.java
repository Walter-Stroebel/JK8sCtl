/*
 * Copyright (c) 2015 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.basicutils;

/**
 * Format a double as a label.
 *
 * @author walter
 */
public interface FormatDouble {
    /**
     * Assume the value is a long and return as %d.
     */
    public static FormatDouble FormatInteger = new FormatDouble() {
        @Override
        public String fmt(double d) {
            return String.format("%d", Math.round(d));
        }
    };
    public static FormatDouble FormatGeneric = new FormatDouble() {
        @Override
        public String fmt(double d) {
            return String.format("%g", d);
        }
    };

    public String fmt(final double d);
}
