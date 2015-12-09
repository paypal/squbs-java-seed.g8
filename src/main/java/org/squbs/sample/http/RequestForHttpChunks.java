package org.squbs.sample.http;

import akka.actor.ActorRef;
import org.squbs.sample.app.PingRequest;

public class RequestForHttpChunks {

    public final PingRequest pingRequest;
    public final int delay;
    public final ActorRef responder;

    public RequestForHttpChunks(PingRequest pingRequest, int delay, ActorRef responder) {
        this.pingRequest = pingRequest;
        this.delay = delay;
        this.responder = responder;
    }
}
