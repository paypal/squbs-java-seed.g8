package org.squbs.sample.app;

/**
 * Ping request message. Classes representing message types are simple and should be immutable.
 */
public class PingRequest {

    public final String who;

    public PingRequest(String who) {
        this.who = who;
    }
}
