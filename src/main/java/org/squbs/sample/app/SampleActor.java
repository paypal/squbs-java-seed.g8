package org.squbs.sample.app;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.squbs.sample.app.AppConstants.*;

/**
 * This is the actor that handles the request messages.
 */
public class SampleActor extends AbstractActor {

    public SampleActor() {
        receive(ReceiveBuilder.
                match(PingRequest.class, r -> {
                    if (r.who.trim().length() > 0) {
                        sender().tell(new PingResponse("Hello " + r.who + " welcome to squbs!"), self());
                    } else {
                        sender().tell(emptyRequest, self());
                    }
                    context().stop(self());
                }).
                match(ChunkRequest.class, r -> {
                    ActorRef requester = sender(); // Save the requester for use in the scheduler.
                    String[] responseArray = {"Hello ", r.who, " welcome ", "to ", "squbs!"};
                    Stream<String> responseStream = Arrays.stream(responseArray);
                    if (r.delay.toMillis() > 0) {
                        Iterator<String> responses = responseStream.iterator();
                        Cancellable scheduler = context().system().scheduler().schedule(r.delay, r.delay, () -> {
                            if (responses.hasNext()) {
                                requester.tell(new PingResponse(responses.next()), self());
                            } else {
                                requester.tell(chunkEnd, self());
                                self().tell(chunkEnd, self());
                            }
                        }, context().dispatcher());
                        context().become(cancelReceive(scheduler));
                    } else {
                        responseStream.forEach(rsp -> requester.tell(new PingResponse(rsp), self()));
                        requester.tell(chunkEnd, self());
                        context().stop(self());
                    }
                }).
                matchAny(r -> context().stop(self())).
                build());
    }

    private PartialFunction<Object, BoxedUnit> cancelReceive(Cancellable scheduler) {
        return ReceiveBuilder.matchEquals(chunkEnd, ce -> {
            scheduler.cancel();
            context().stop(self());
        }).build();
    }
}