package no.java.ems.server.cli;

import no.java.ems.server.Stoppable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShutdownUtil {
    public static void waitForShutdown(final Stoppable stoppable) {

        final AtomicBoolean stop = new AtomicBoolean();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.err.println("Got shutdown signal...");

                synchronized (stop) {
                    stop.set(true);
                    stop.notifyAll();
                }
            }
        }));

        synchronized (stop) {
            while (!stop.get()) {
                try {
                    stop.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        try {
            stoppable.stop();
        } catch (Exception e) {
            System.err.println("Error while shutting down server.");
            e.printStackTrace(System.err);
        }
    }
}
