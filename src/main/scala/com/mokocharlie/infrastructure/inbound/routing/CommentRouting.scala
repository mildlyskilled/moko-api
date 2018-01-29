package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.service.CommentService

class CommentRouting(commentService: CommentService) extends SprayJsonSupport with JsonConversion {

  var routes: Route = {
    path("comments") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNum, limit) ⇒
          {
            onSuccess(commentService.mostRecentComments(pageNum, limit, None)) {
              case Right(page) ⇒ complete(page)
              case Left(error) ⇒ complete(StatusCodes.InternalServerError, error.msg)
            }
          }
        }
      }
    } ~
      path("comments" / LongNumber) { id ⇒
        {
          onSuccess(commentService.commentById(id)) {
            case Right(comment) ⇒ complete(comment)
            case Left(error) ⇒ complete(StatusCodes.NotFound, error.msg)
          }
        }
      }
  } ~ path("comments" / "photo" / LongNumber) { id =>
    get {
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
        {
          val commentsFuture = commentService.commentsByImage(id, pageNumber, limit, None)
          onSuccess(commentsFuture) {
            case Right(page) ⇒ complete(page)
            case Left(e) ⇒ complete(e.msg)
          }
        }
      }
    }
  }
}
