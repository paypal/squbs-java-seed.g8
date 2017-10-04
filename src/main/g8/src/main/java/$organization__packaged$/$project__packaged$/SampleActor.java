package $organization$.$project$;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.Attributes;
import akka.stream.DelayOverflowStrategy;
import akka.stream.javadsl.Source;

import java.util.Arrays;

import static $organization$.$project$.AppConstants.emptyRequest;

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
                Source<PingResponse, NotUsed> source = Source.from(Arrays.asList(responseArray))
                    .map(elem -> new PingResponse(elem))
                    .delay(r.delay, DelayOverflowStrategy.backpressure());

                requester.tell(new ChunkSourceMessage(source), self());
            }).
            matchAny(r -> context().stop(self())).
            build());
    }

}
