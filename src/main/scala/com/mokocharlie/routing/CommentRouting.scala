package com.mokocharlie.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import ch.megard.akka.http.cors.CorsDirectives._
import com.mokocharlie.Marshalling
import com.mokocharlie.repository.CommentRepository

object CommentRouting extends CommentRepository with Marshalling {

  var routes: Route = cors() {
    path ("photos" / LongNumber / "comments") { id =>
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val commentsFuture = CommentDAO.findCommentsByImageID(id, pageNumber, limit)
            onSuccess(commentsFuture) {
              case page => complete(page)
            }
          }
        }
      }
    }

  }
}
