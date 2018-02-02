package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.common.RequestEntity.AuthRequest
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.UserService
import com.typesafe.scalalogging.StrictLogging

class UserRouting(userService: UserService)
    extends JsonConversion
    with SprayJsonSupport
    with HttpErrorMapper
    with StrictLogging {

  val routes: Route = {
    path("users" / LongNumber) { id =>
      get {
        onSuccess(userService.userById(id)) {
          case Right(user) ⇒ complete(user)
          case Left(error) ⇒ completeWithError(error)
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
        get{
          onSuccess(userService.refreshToken(refreshToken)) {
            case Right(token) ⇒ complete(token)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
  }
}
