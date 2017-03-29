package org.squbs.sample;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
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
    static ActorMaterializer mat;

    @BeforeClass
    public static void beforeAll() {
        system = ActorSystem.create("SampleActorTest");
        mat = ActorMaterializer.create(system);
    }

    @AfterClass
    public static void afterAll() {
        system.terminate();
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
            FiniteDuration interval = Duration.create(200, TimeUnit.MILLISECONDS);
            target.tell(new ChunkRequest("foo", interval), getRef());

            ChunkSourceMessage chunkMsg = expectMsgClass(timeout, ChunkSourceMessage.class);
            chunkMsg.source.runWith(Sink.actorRef(getRef(), "Done!"), mat);

            chunks.forEach(chunk -> {
                PingResponse resp = expectMsgClass(timeout, PingResponse.class);
                assertEquals(chunk, resp.message);
            });
            expectMsgEquals("Done!");
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
