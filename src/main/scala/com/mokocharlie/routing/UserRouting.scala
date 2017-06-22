package com.mokocharlie.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mokocharlie.Marshalling
import com.mokocharlie.repository.UserRepository

object UserRouting extends UserRepository with Marshalling {

  val routes: Route = cors() {
    path("users" / LongNumber) { id =>
      get {
        val userFuture = UserDAO.findUserByID(id)
        onSuccess(userFuture)(user => complete(user))
      }
    }
  }
}
