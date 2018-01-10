package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.UserRepository

class UserRouting(repo: UserRepository)
  extends JsonConversion
    with SprayJsonSupport {

  val routes: Route = {
    path("users" / LongNumber) { id =>
      get {
        val userFuture = repo.findUserByID(id)
        onSuccess(userFuture)(user => complete(user))
      }
    }
  }
}
