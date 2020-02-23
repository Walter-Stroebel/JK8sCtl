/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8salert;

import java.util.LinkedList;

/**
 *
 * @author walter
 */
public abstract class AbstractAlerter implements Alerter {

    public static class TimedMessage {

        public long at;
        public long cnt;
        public String msg;

        public TimedMessage(String msg) {
            this.msg = msg;
            at = System.currentTimeMillis();
            cnt = 1;
        }
    }
    protected final AlertSinkConfig config;
    private long lastEmit;
    protected final LinkedList<TimedMessage> queued = new LinkedList<>();

    public AbstractAlerter(AlertSinkConfig cfg) {
        this.config = cfg;
        this.lastEmit = System.currentTimeMillis();
    }

    public int emit(String msg) {
        if (null != msg) {
            boolean dup = false;
            if (config.squelchDuplicates) {
                for (TimedMessage m : queued) {
                    if (m.msg.equals(msg)) {
                        dup = true;
                        m.cnt++;
                        break;
                    }
                }
            }
            if (!dup) {
                queued.add(new TimedMessage(msg));
                if (queued.size() > config.discardOnNumberQueued) {
                    queued.removeFirst();
                }
            }
        }
        double allowed = System.currentTimeMillis() - lastEmit;// ms since last emit
        allowed /= 1000;//seconds since last emit
        allowed *= config.messagesPerSecond;
        int nmsg = (int) Math.round(allowed);
        if (nmsg >= 1) {
            lastEmit = System.currentTimeMillis();
            //System.out.println("Allowing " + nmsg + " messages");
        }
        return nmsg;
    }
}
