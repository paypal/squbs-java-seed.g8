package org.squbs.sample.app;

/**
 * Ping response message. Classes representing message types are simple and should be immutable.
 */
public class PingResponse {

    public final String message;

    public PingResponse(String message) {
        this.message = message;
    }
}
