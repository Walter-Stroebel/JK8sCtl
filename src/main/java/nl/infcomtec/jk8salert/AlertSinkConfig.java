/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8salert;

/**
 * Holds a very generic description and configuration of some Alert destination.
 * This can be a service that will send an SMS (Short Text Message), calls an
 * URL of your corporate monitoring system, pops up a message and so on.
 * <p>
 * Which fields need to be filled in depends mostly on the implementation of the
 * Alerter, only 'name' and 'type' are required. 'messagesPerSecond' and
 * 'discardOnNumberQueued' are also required but have a default value.
 *
 * @author walter
 */
public class AlertSinkConfig implements Comparable<Object> {

    /**
     * Must be a unique name (case-insensitive) and should be human-descriptive.
     * For instance "Send SMS to John", "Pop-up alert".
     */
    public String name;
    /**
     * The alerter to use with this configuration. This is a simple Java class
     * name; the class must exist in the jar at the appropriate path for
     * security reasons.
     */
    public String type;
    /**
     * Destination of the alert. Can be email address(es), mobile phone
     * number(s) and so on.
     */
    public String[] destinations;
    /**
     * Squelch (suppress) duplicate messages.
     */
    public boolean squelchDuplicates = true;
    /**
     * Rate limiter to avoid being spammed to death and to avoid the alerter
     * itself crashing in the case of a major disaster, because the sink cannot
     * handle that many messages. Defaults to 1 message per minute.
     */
    public double messagesPerSecond = 1.0 / 60.0;
    /**
     * Defines what to do if more than the rate limit messages arrive. Default
     * is one message per minute and queue 60 messages; which would spam you for
     * up to an hour.
     */
    public int discardOnNumberQueued = 60;
    /**
     * If the sink needs URL(s), place them here.
     */
    public String[] url;
    /**
     * If the sink needs address(es), for instance the host name or IP address
     * of a mail server, place them here. Could also be the name of a file to
     * log to or a (named) socket or pipe.
     */
    public String[] address;
    /**
     * If the sink needs to authenticate, place the authentication details here.
     */
    public String[] authentication;
    /**
     * Allows to chain alerters.
     * <p>
     * For instance, the first alerter could write to a log file (with a very
     * large messagesPerSecond) and then forward to email (accepting a few 100
     * messages) which in turn could forward to SMS allowing one message per 5
     * minutes.
     * <p>
     * Multiple forwarding will fan-out a message to multiple sinks. This allows
     * for some fault-tolerance.
     */
    public String[] chainAlertSinks;

    @Override
    public int compareTo(Object o) {
        if (o instanceof AlertSinkConfig) {
            return name.compareToIgnoreCase(((AlertSinkConfig) o).name);
        } else if (o instanceof String) {
            return name.compareToIgnoreCase((String) o);
        } else {
            return name.compareToIgnoreCase(o.toString());
        }
    }
}
