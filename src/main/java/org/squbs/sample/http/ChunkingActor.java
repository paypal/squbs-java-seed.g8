package org.squbs.sample.http;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import org.squbs.pattern.spray.japi.ChunkedMessageEndFactory;
import org.squbs.pattern.spray.japi.HttpHeaderFactory;
import org.squbs.pattern.spray.japi.HttpResponseBuilder;
import org.squbs.pattern.spray.japi.MessageChunkFactory;
import org.squbs.sample.app.AppConstants;
import org.squbs.sample.app.ChunkRequest;
import org.squbs.sample.app.PingResponse;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;
import spray.http.ChunkedResponseStart;

import java.util.concurrent.TimeUnit;

import static org.squbs.sample.app.AppConstants.*;

/**
 * Warning: Advanced Topic - response chunking.
 * The chunking actor. We only need this one in case of chunking.
 */
public class ChunkingActor extends AbstractActorWithStash {

    private static final String ack = "ack";

    public ChunkingActor() {
        receive(ReceiveBuilder.
                match(RequestForHttpChunks.class, r -> {
                    context().actorSelection(AppConstants.pingActorPath).tell(new ChunkRequest(
                            r.pingRequest.who, Duration.create(r.delay, TimeUnit.MILLISECONDS)), self());
                    r.responder.tell(new ChunkedResponseStart(new HttpResponseBuilder()
                            .header(HttpHeaderFactory.create("content-type", "text/plain")).build())
                            .withAck(ack), self());
                    context().become(ackWait(chunkEmitter(r.responder)));
                }).build());
    }

    private PartialFunction<Object, BoxedUnit> ackWait(PartialFunction<Object, BoxedUnit> sendAwait) {
        return ReceiveBuilder.
                matchEquals(ack, a -> {
                    unstashAll();
                    context().become(sendAwait, false);
                }).
                matchAny(a -> stash()).
                build();
    }

    private PartialFunction<Object, BoxedUnit> chunkEmitter(ActorRef responder) {
        return ReceiveBuilder.
                match(PingResponse.class, r -> {
                    responder.tell(MessageChunkFactory.create(r.message).withAck(ack), self());
                    context().unbecome();
                }).
                matchEquals(chunkEnd, e -> {
                    responder.tell(ChunkedMessageEndFactory.create(), self());
                    context().stop(self());
                }).
                build();
    }
}
