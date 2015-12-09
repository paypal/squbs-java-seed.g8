package org.squbs.sample.http

import akka.actor.Props
import akka.pattern._
import akka.util.Timeout
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.squbs.httpclient.json.JacksonProtocol
import org.squbs.sample.app.{AppConstants, PingRequest, PingResponse}
import org.squbs.unicomplex.RouteDefinition
import spray.http._
import spray.routing.Directives._
import spray.routing._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

/**
  * The route definition.
  */
class SampleHttpSvc extends RouteDefinition {

  import context.dispatcher

  implicit val timeout: Timeout = 3 seconds

  import AppConstants._

  // This module allows unmarshalling immutable Java objects by constructor, without annotations.
  JacksonProtocol.defaultMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))

  override def route: Route =
    get {
      path("hello") {
        onComplete(context.actorSelection(pingActorPath) ? new PingRequest("anonymous")) {
          case Success(p: PingResponse) => complete(p.message)
          case _ => complete(StatusCodes.BadRequest)

        }
      } ~
      path("hello" / Segment) { who =>
        implicit val marshaller = JacksonProtocol.jacksonMarshaller(classOf[PingResponse])
        onComplete(context.actorSelection(pingActorPath) ? new PingRequest(who)) {
          case Success(response: PingResponse) => complete(response)
          case _ => complete(StatusCodes.BadRequest)
        }
      } ~
      path("hello" / Segment / IntNumber) { (who, delay) => ctx =>
        context.actorOf(Props[ChunkingActor]) ! new RequestForHttpChunks(new PingRequest(who), delay, ctx.responder)
      }
    } ~
    post {
      path("hello") {
        implicit val unmarshaller = JacksonProtocol.jacksonUnmarshaller(classOf[PingRequest])
        implicit val marshaller = JacksonProtocol.jacksonMarshaller(classOf[PingResponse])
        entity(as[PingRequest]) { request =>
          onComplete(context.actorSelection(pingActorPath) ? request) {
            case Success(response: PingResponse) => complete(response)
            case _ => complete(StatusCodes.BadRequest)
          }
        }
      }
    } ~
    complete("Hello!")
}
