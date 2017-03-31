package org.squbs.sample;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import org.junit.After;
import org.junit.Test;
import org.squbs.testkit.japi.CustomTestKit;

import static org.junit.Assert.assertEquals;

public class SampleHttpServiceTest extends CustomTestKit {

    static final int TEST_TIMEOUT = 30000;

    final Materializer mat = ActorMaterializer.create(super.system());

    final Http http = Http.get(super.system());

    public SampleHttpServiceTest() {
        super(true);
    }

    @After
    public void cleanup() {
        super.shutdown();
    }

    @Test
    public void testGetSampleHttpSvcNegWithSpace() throws Exception {
        final HttpResponse resp = http
            .singleRequest(HttpRequest.create("http://localhost:" + port() + "/hello/%20"), mat)
            .toCompletableFuture().get();
        assertEquals(StatusCodes.BAD_REQUEST, resp.status());
    }

    @Test
    public void testGetSampleHttpSvc() throws Exception {
        final HttpResponse resp = http
            .singleRequest(HttpRequest.create("http://localhost:" + port() + "/hello"),
                mat).toCompletableFuture().get();

        HttpEntity.Strict entity = resp.entity().toStrict(TEST_TIMEOUT, mat).toCompletableFuture().get();
        String responseString = entity.getData().utf8String();

        assertEquals(StatusCodes.OK, resp.status());
        assertEquals("\"Hello anonymous welcome to squbs!\"", responseString);
    }

    @Test
    public void testGetSampleHttpSvcWithName() throws Exception {
        String name = "Joe";
        final HttpResponse resp = http
            .singleRequest(HttpRequest.create("http://localhost:" + port() + "/hello/" + name),
                mat).toCompletableFuture().get();

        HttpEntity.Strict entity = resp.entity().toStrict(TEST_TIMEOUT, mat).toCompletableFuture().get();
        String respStr = entity.getData().utf8String();
        assertEquals(StatusCodes.OK, resp.status());
        assertEquals("{\"message\":\"Hello Joe welcome to squbs!\"}", respStr);
    }

    @Test
    public void testRootContextReturnsHello() throws Exception {
        final HttpResponse resp = http
            .singleRequest(HttpRequest.create("http://localhost:" + port() + "/"), mat)
            .toCompletableFuture().get();

        HttpEntity.Strict entity = resp.entity().toStrict(TEST_TIMEOUT, mat).toCompletableFuture().get();
        String respStr = entity.getData().utf8String();
        assertEquals("Hello!", respStr);
    }

    @Test
    public void testChunkNoDelay() throws Exception {
        final HttpResponse resp = http
            .singleRequest(HttpRequest.create("http://localhost:" + port() + "/hello/foo/0"), mat)
            .toCompletableFuture().get();
        HttpEntity.Strict entity = resp.entity().toStrict(TEST_TIMEOUT, mat).toCompletableFuture().get();
        String respStr = entity.getData().utf8String();

        assertEquals(StatusCodes.OK, resp.status());
        assertEquals("Hello foo welcome to squbs!", respStr);
    }

    @Test
    public void testPostHello() throws Exception {
        final HttpResponse resp = http
            .singleRequest(HttpRequest.POST("http://localhost:" + port() + "/hello").withEntity(
                    ContentTypes.APPLICATION_JSON, "{\"who\":\"Bar\"}"
            ), mat)
            .toCompletableFuture().get();

        assertEquals(StatusCodes.OK, resp.status());
        HttpEntity.Strict entity = resp.entity().toStrict(TEST_TIMEOUT, mat).toCompletableFuture().get();
        String respStr = entity.getData().utf8String();
        assertEquals("{\"message\":\"Hello Bar welcome to squbs!\"}", respStr);
    }
}
