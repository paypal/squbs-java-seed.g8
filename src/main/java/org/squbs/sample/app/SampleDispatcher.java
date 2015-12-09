package org.squbs.sample.app;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * The dispatcher serves as a singleton registered entry point. It creates/manages actors to handle
 * the actual request and allows multiple access methods to this service. Only HTTP is shown
 * but it would be rather simple to add other access methods like messaging/streams, etc.
 * We could use actors with routers or any other method that has a static entry point, instead.
 */
public class SampleDispatcher extends AbstractActor {

    public SampleDispatcher() {
        receive(ReceiveBuilder.
                matchAny(r -> context().actorOf(Props.create(SampleActor.class)).forward(r, context())).
                build());
    }
}
