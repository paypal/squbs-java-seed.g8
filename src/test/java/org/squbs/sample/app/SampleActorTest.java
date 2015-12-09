package org.squbs.sample.app;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class SampleActorTest {

    static ActorSystem system;
    static FiniteDuration timeout = Duration.create(1, TimeUnit.SECONDS);

    @BeforeClass
    public static void beforeAll() {
        system = ActorSystem.create("SampleActorTest");
    }

    @AfterClass
    public static void afterAll() {
        system.shutdown();
    }

    @Test
    public void testPingRequest() {
        new JavaTestKit(system) {{
            ActorRef target = system.actorOf(Props.create(SampleActor.class));
            watch(target);
            target.tell(new PingRequest("foo"), getRef());
            PingResponse response = expectMsgClass(timeout, PingResponse.class);
            assertEquals("Hello foo welcome to squbs!", response.message);
            expectTerminated(timeout, target);
        }};
    }

    @Test
    public void testChunkRequest() {
        new JavaTestKit(system) {{
            String[] chunkArray = {"Hello ", "foo", " welcome ", "to ", "squbs!"};
            Stream<String> chunks = Arrays.stream(chunkArray);

            ActorRef target = getSystem().actorOf(Props.create(SampleActor.class));
            watch(target);
            FiniteDuration interval = Duration.create(200, TimeUnit.MILLISECONDS);
            target.tell(new ChunkRequest("foo", interval), getRef());
            chunks.forEach(chunk -> {
                PingResponse r = expectMsgClass(timeout, PingResponse.class);
                assertEquals(chunk, r.message);
            });
            expectMsgEquals(timeout, AppConstants.chunkEnd);
            expectTerminated(timeout, target);
        }};
    }

    @Test
    public void testInvalidRequest() {
        new JavaTestKit(system) {{
            ActorRef target = getSystem().actorOf(Props.create(SampleActor.class));
            watch(target);
            target.tell("other", getRef());
            expectTerminated(timeout, target);
        }};
    }
}
