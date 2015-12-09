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

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SampleDispatcherTest {

    static ActorSystem system;
    static FiniteDuration timeout = Duration.create(1, TimeUnit.SECONDS);

    @BeforeClass
    public static void beforeAll() {
        system = ActorSystem.create("SampleDispatcherTest");
    }

    @AfterClass
    public static void afterAll() {
        system.shutdown();
    }

    @Test
    public void testForwardRequest() {
        new JavaTestKit(system) {{
            ActorRef target = getSystem().actorOf(Props.create(SampleDispatcher.class), "SampleDispatcher");
            target.tell(new PingRequest("foo"), getRef());
            PingResponse response = expectMsgClass(timeout, PingResponse.class);
            assertEquals("Hello foo welcome to squbs!", response.message);
            String senderPath = getLastSender().path().toString();
            System.out.println(senderPath);
            assertTrue(senderPath.matches("akka://SampleDispatcherTest/user/SampleDispatcher/\\$\\w"));
        }};
    }
}
