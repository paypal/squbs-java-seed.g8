package org.squbs.sample.http;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import org.squbs.sample.app.SampleDispatcher;

/**
 * The MockSupervisor mocks a cube supervisor and starts the target actor without starting the
 * squbs infrastructure.
 */
public class MockSupervisor extends AbstractActor {

    public MockSupervisor() {
        context().actorOf(Props.create(SampleDispatcher.class), "sample");
        receive(ReceiveBuilder.matchAny(r -> {}).build());
    }
}
