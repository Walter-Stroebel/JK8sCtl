/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8salert;

/**
 *
 * @author walter
 */
public class ConsoleAlerter extends AbstractAlerter {

    public ConsoleAlerter(AlertSinkConfig cfg) {
        super(cfg);
        System.out.println("ConsoleAlerter loaded.");
    }

    @Override
    public void error(String msg) {
        int n = emit(String.format("ERROR: %s", msg));
        output(n);
    }

    private void output(int n) {
        while (n > 0 && !queued.isEmpty()) {
            TimedMessage tm = queued.removeFirst();
            System.out.format("%1$tF %1$tT%2$s %3$s\n", tm.at, tm.cnt > 1 ? " (" + tm.cnt + ")" : "", tm.msg);
            n--;
        }
    }

    @Override
    public void info(String msg) {
        int n = emit(String.format("INFO: %s", msg));
        output(n);
    }

    @Override
    public void poll() {
        int n = emit(null);
        output(n);
    }

    @Override
    public void warning(String msg) {
        int n = emit(String.format("WARNING: %s", msg));
        output(n);
    }
}
