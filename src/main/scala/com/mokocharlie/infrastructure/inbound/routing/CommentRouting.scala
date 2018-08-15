package com.mokocharlie.infrastructure.inbound.routing

import java.sql.Timestamp
import java.time.{Clock, Instant}

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.domain.MokoModel.Comment
import com.mokocharlie.domain.common.RequestEntity.CommentRequest
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.HeaderChecking
import com.mokocharlie.service.{CommentService, PhotoService, UserService}

import scala.concurrent.ExecutionContextExecutor

class CommentRouting(commentService: CommentService, override val userService: UserService, photoService: PhotoService, clock: Clock)(
    implicit val system: ActorSystem)
    extends SprayJsonSupport
    with HttpUtils
    with HeaderChecking {

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  import JsonConversion._

  var routes: Route = {
    path("comments") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNum, limit) ⇒
          optionalHeaderValue(extractUserToken) { user ⇒
            user
              .map { userResponse ⇒
                val res = for {
                  u ← userResponse.user
                  comments ← commentService.mostRecentComments(
                    pageNum,
                    limit,
                    userService.publishedFlag(u))
                } yield comments

                onSuccess(res) {
                  case Right(page) ⇒ complete(page)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
              .getOrElse {
                onSuccess(commentService.mostRecentComments(pageNum, limit, Some(true))) {
                  case Right(commentPage) ⇒ complete(commentPage)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
          }
        }
      }
    } ~
      path("comments" / LongNumber) { id ⇒
        get {
          onSuccess(commentService.commentById(id)) {
            case Right(comment) ⇒ complete(comment)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
  } ~ path("comments" / "photo" / LongNumber) { id =>
    get {
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
        optionalHeaderValue(extractUserToken) { user ⇒
          {
            user
              .map { tokenResponse ⇒
                val res = for {
                  u ← tokenResponse.user
                  comments ← commentService.commentsByImage(
                    id,
                    pageNumber,
                    limit,
                    userService.publishedFlag(u))
                } yield comments

                onSuccess(res) {
                  case Right(page) ⇒ complete(page)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
              .getOrElse {
                onSuccess(commentService.commentsByImage(id, pageNumber, limit, Some(true))) {
                  case Right(page) ⇒ complete(page)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
          }
        }
      }
    }
  } ~ path("comments" / "album" / LongNumber) { id ⇒
    get {
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
        optionalHeaderValue(extractUserToken) { user ⇒
          {
            user
              .map { tokenResponse ⇒
                val res = for {
                  u ← tokenResponse.user
                  comments ← commentService.commentsByAlbum(
                    id,
                    pageNumber,
                    limit,
                    userService.publishedFlag(u))
                } yield comments

                onSuccess(res) {
                  case Right(page) ⇒ complete(page)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
              .getOrElse {
                onSuccess(commentService.commentsByAlbum(id, pageNumber, limit, Some(true))) {
                  case Right(page) ⇒ complete(page)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
          }
        }
      }
    }
  } ~ path("comments") {
    post {entity(as[CommentRequest]) { commentRequest ⇒
      optionalHeaderValue(extractUserToken) {user ⇒
        user.map { tokenResponse ⇒
          val res = for {
            user ← tokenResponse.user
            photo ← photoService.photoById(commentRequest.photoId)
            post ← commentService.createOrUpdate(Comment(0l, photo, commentRequest.comment,commentRequest.author, Timestamp.from(Instant.now(clock)), true)) if (user.exists(_.id == commentRequest.userId))
          } yield post

          onSuccess(res) {
            case Right(id) ⇒ complete(StatusCodes.Accepted, s"Posted comment $id")
            case Left(error) ⇒ completeWithError(error)
          }
        }
        }
      }
    }
  }
}
