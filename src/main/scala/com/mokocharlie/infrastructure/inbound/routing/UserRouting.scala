package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.UserRepository

object UserRouting
  extends UserRepository
    with JsonConversion
    with SprayJsonSupport {

  val routes: Route = cors() {
    path("users" / LongNumber) { id =>
      get {
        val userFuture = UserDAO.findUserByID(id)
        onSuccess(userFuture)(user => complete(user))
      }
    }
  }
}
