package org.squbs.sample;

/**
 * Ping request message. Classes representing message types are simple and should be immutable.
 */
public class PingRequest {

    public final String who;

    //default constructor required for Jackson
    public PingRequest() { who = ""; }

    public PingRequest(String who) {
        this.who = who;
    }
}
