package org.squbs.sample;

import akka.NotUsed;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import akka.util.Timeout;
import org.squbs.actorregistry.japi.ActorLookup;
import org.squbs.unicomplex.AbstractRouteDefinition;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.server.PathMatchers.segment;

/**
 * The route definition.
 */
public class SampleHttpService extends AbstractRouteDefinition {

    final ActorLookup<?> lookup = ActorLookup.create(context());
    final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));

    private Route anonymous = route(
        path("hello", () -> pathEnd(() ->
            onComplete(lookup.ask(new PingRequest("anonymous"), timeout).thenApply(PingResponse.class::cast),
                response -> {
                    if (response.isSuccess()) {
                        return complete(StatusCodes.OK, response.get().message, Jackson.marshaller());
                    } else {
                        return complete(StatusCodes.BAD_REQUEST);
                    }
                })
        )));

    private Route withName = route(
        path(segment("hello").slash(PathMatchers.segment()), who -> pathEnd(() ->
            onComplete(lookup.ask(new PingRequest(who), timeout).thenApply(PingResponse.class::cast),
                response -> {
                    if (response.isSuccess()) {
                        return complete(StatusCodes.OK, response.get(), Jackson.marshaller());
                    } else {
                        return complete(StatusCodes.BAD_REQUEST);
                    }
                }
            )
        )));

    private Route chunks = route(
        path(segment("hello").slash(PathMatchers.segment().slash(PathMatchers.integerSegment())), (who, delay) ->
            // This header is added for Chrome to handle chunking responses.  Please see
            // http://stackoverflow.com/questions/26164705/chrome-not-handling-chunked-responses-like-firefox-safari
            respondWithHeader(RawHeader.create("X-Content-Type-Options", "nosniff"), () ->

                onComplete(lookup.ask(
                    new ChunkRequest(who, FiniteDuration.create(delay, TimeUnit.MILLISECONDS)), timeout)
                        .thenApply(ChunkSourceMessage.class::cast),
                    response -> {
                        if (response.isSuccess()) {
                            Source<ByteString, NotUsed> byteSource = response.get().source
                                .map(e -> ByteString.fromString(e.message))
                                .concat(Source.single(ByteString.empty()));

                            return complete(HttpEntities.createChunked(ContentTypes.TEXT_PLAIN_UTF8, byteSource));
                        } else {
                            return complete(StatusCodes.BAD_REQUEST);
                        }
                    }
                )
            )
        )
    );

    @Override
    public Route route() {

        return route(
            get(() -> route(anonymous, withName, chunks)),
            post(() -> path("hello", () ->
                entity(Jackson.unmarshaller(PingRequest.class), request ->
                    onComplete(lookup.ask(request, timeout).thenApply(PingResponse.class::cast),
                        response -> {
                            if (response.isSuccess()) {
                                return complete(StatusCodes.OK, response.get(), Jackson.marshaller());
                            } else {
                                return complete(StatusCodes.BAD_REQUEST);
                            }
                        }))
                )
            ),
            complete("Hello!")
        );
    }

}
