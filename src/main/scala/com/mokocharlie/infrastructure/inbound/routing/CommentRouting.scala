package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mokocharlie.domain.Comment
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.CommentRepository

import scala.concurrent.Future

object CommentRouting
  extends CommentRepository
    with SprayJsonSupport
    with JsonConversion {

  var routes: Route = cors() {
    path("comments") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (page, limit) => {
            val commentFuture = CommentDAO.getMostRecent(page, limit)
            onSuccess(commentFuture)(page => complete(page))
          }
        }
      }
    } ~
      path("comments" / LongNumber) {
        id => {
          val commentFuture: Future[Option[Comment]] = CommentDAO.findCommentByID(id)
          onSuccess(commentFuture) {
            case Some(comment) => complete(comment)
            case None => complete(StatusCodes.NotFound)
          }
        }
      }
  }
}
