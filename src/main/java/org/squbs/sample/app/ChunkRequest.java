package org.squbs.sample.app;

import scala.concurrent.duration.FiniteDuration;

/**
 * Chunk request message. Classes representing message types are simple and should be immutable.
 */
public class ChunkRequest {

    public final String who;
    public final FiniteDuration delay;

    public ChunkRequest(String who, FiniteDuration delay) {
        this.who = who;
        this.delay = delay;
    }
}
