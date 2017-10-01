package $organization$.$project$;

import akka.actor.AbstractActor;
import akka.actor.Props;
import $organization$.$project$.SampleDispatcher;

/**
 * The MockSupervisor mocks a cube supervisor and starts the target actor without starting the
 * squbs infrastructure.
 */
public class MockSupervisor extends AbstractActor {

    public MockSupervisor() {
        context().actorOf(Props.create(SampleDispatcher.class), "sample");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(r -> {}).build();
    }
}
