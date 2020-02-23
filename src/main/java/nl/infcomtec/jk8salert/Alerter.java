/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8salert;

/**
 *
 * @author walter
 */
public interface Alerter {
    void warning(String msg);
    void error(String msg);
    void info(String msg);
    void poll();
}
