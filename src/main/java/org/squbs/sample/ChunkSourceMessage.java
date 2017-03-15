package org.squbs.sample;

import akka.NotUsed;
import akka.stream.javadsl.Source;

/**
 * Message type used for chunking response Source. Class representing message type between actor and service and should
 * be immutable.
 */
public class ChunkSourceMessage {

    public final Source<PingResponse, NotUsed> source;

    public ChunkSourceMessage(Source<PingResponse, NotUsed> responseSource) {
        source = responseSource;
    }
}
