package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.UserService

class UserRouting(userService: UserService)
    extends JsonConversion
    with SprayJsonSupport
    with HttpErrorMapper {

  val routes: Route = {
    path("users" / LongNumber) { id =>
      get {
        val userFuture = userService.userById(id)
        onSuccess(userFuture) {
          case Right(user) ⇒ complete(user)
          case Left(error) ⇒ completeWithError(error)
        }
      }
    }
  }
}
