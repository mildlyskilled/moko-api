package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.common.MokoCharlieServiceError.OperationDisallowed
import com.mokocharlie.domain.common.RequestEntity.AuthRequest
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.HeaderChecking
import com.mokocharlie.service.UserService
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContextExecutor, Future}

class UserRouting(override val userService: UserService)(implicit system: ActorSystem)
    extends SprayJsonSupport
    with HttpUtils
    with StrictLogging
    with HeaderChecking {

  import JsonConversion._
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val routes: Route = {

    path("users" / LongNumber) { id =>
      get {
        optionalHeaderValue(extractUserToken) { userOption ⇒
          userOption.map { userFuture ⇒
            val res = for {
              u ← userFuture
              f ← if (u.exists(_.isSuperuser)) userService.userById(id)
              else Future.successful(Left(OperationDisallowed("You need to be a super user")))
            } yield f

            onSuccess(res) {
              case Right(foundUser) ⇒ complete(foundUser)
              case Left(error) ⇒ completeWithError(error)
            }
          }.getOrElse(
            completeWithError(OperationDisallowed("Could not retrieve user without a token"))
          )
        }
      }
    } ~
      path("auth" ~ Slash.?) {
        post {
          entity(as[AuthRequest]) { authRequest ⇒
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
