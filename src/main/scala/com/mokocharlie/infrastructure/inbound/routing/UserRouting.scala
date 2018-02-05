package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.common.MokoCharlieServiceError.OperationDisallowed
import com.mokocharlie.domain.common.RequestEntity.AuthRequest
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.spartan.HotGate
import com.mokocharlie.service.UserService
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

class UserRouting(override val userService: UserService)(implicit system: ActorSystem)
    extends JsonConversion
    with SprayJsonSupport
    with HttpErrorMapper
    with StrictLogging
    with HotGate {

  implicit val ec = system.dispatcher

  val routes: Route = {
    path("users" / LongNumber) { id =>
      get {
        extractUser { user ⇒
          val res = for {
            u ← user
            f ← if (u.exists(_.isSuperuser)) userService.userById(id)
                else Future.successful(Left(OperationDisallowed("You need to be a super user")))
          } yield f

          onSuccess(res) {
            case Right(foundUser) ⇒ complete(foundUser)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
    } ~
      path("auth" ~ Slash.?) {
        post {
          entity(as[AuthRequest]) { (authRequest) ⇒
            logger.info(s"Got request from ${authRequest.email}")
            onSuccess(userService.auth(authRequest.email, authRequest.password)) {
              case Right(token) ⇒ complete(token)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      } ~
      path("refresh" / Segment) { refreshToken ⇒
        get {
          onSuccess(userService.refreshToken(refreshToken)) {
            case Right(token) ⇒ complete(token)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
  }
}
