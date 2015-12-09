package org.squbs.sample.http

import akka.actor.Props
import org.scalatest.{FlatSpecLike, Matchers}
import org.squbs.httpclient.json.JacksonProtocol
import org.squbs.sample.app.{PingRequest, PingResponse}
import org.squbs.testkit.TestRoute
import spray.http.{MessageChunk, StatusCodes}
import spray.testkit.ScalatestRouteTest

import scala.concurrent.duration._
import scala.language.postfixOps

class SampleSvcSpec extends FlatSpecLike with Matchers with ScalatestRouteTest {

  implicit val timeout = RouteTestTimeout(5 seconds)

  val route = TestRoute[SampleHttpSvc]

  system.actorOf(Props[MockSupervisor], "squbs-seed")

  behavior of "the sample route"

  it should "handle simple request correctly" in {
    Get() ~> route ~> check {
      responseAs[String] should be ("Hello!")
    }
  }

  it should "handle path correctly" in {
    Get("/hello") ~> route ~> check {
      responseAs[String] should be ("Hello anonymous welcome to squbs!")
    }
  }

  it should "handle path segment and serialization" in {
    implicit val unmarshaller = JacksonProtocol.jacksonUnmarshaller(classOf[PingResponse])
    Get("/hello/foo") ~> route ~> check {
      responseAs[PingResponse].message should be ("Hello foo welcome to squbs!")
    }
  }

  it should "return bad request for path segment representing space" in {
    Get("/hello/%20") ~> route ~> check {
      status should be (StatusCodes.BadRequest)
    }
  }

  it should "handle path segment, chunking, with delay" in {
    Get("/hello/foo/500") ~> route ~> check {
      val expected = List("Hello ", "foo", " welcome ", "to ", "squbs!") map MessageChunk.apply
      chunks should be (expected)
    }
  }

  it should "handle path segment, chunking, no delay" in {
    Get("/hello/foo/0") ~> route ~> check {
      val expected = List("Hello ", "foo", " welcome ", "to ", "squbs!") map MessageChunk.apply
      chunks should be (expected)
    }
  }

  it should "handle post serialization and deserialization" in {
    implicit val marshaller = JacksonProtocol.jacksonMarshaller(classOf[PingRequest])
    implicit val unmarshaller = JacksonProtocol.jacksonUnmarshaller(classOf[PingResponse])
    Post("/hello", new PingRequest("bar")) ~> route ~> check {
      responseAs[PingResponse].message should be ("Hello bar welcome to squbs!")
    }
  }

  it should "return bad request for request with blank field" in {
    implicit val marshaller = JacksonProtocol.jacksonMarshaller(classOf[PingRequest])
    Post("/hello", new PingRequest("")) ~> route ~> check {
      status should be (StatusCodes.BadRequest)
    }
  }
}
