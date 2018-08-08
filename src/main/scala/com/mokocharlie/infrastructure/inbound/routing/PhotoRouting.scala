package com.mokocharlie.infrastructure.inbound.routing

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.common.MokoCharlieServiceError.AuthenticationError
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.HeaderChecking
import com.mokocharlie.service.{CommentService, PhotoService, UserService}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor

class PhotoRouting(
    photoService: PhotoService,
    commentService: CommentService,
    clock: Clock,
    override val userService: UserService)(implicit system: ActorSystem)
    extends SprayJsonSupport
    with HeaderChecking
    with HttpUtils
    with StrictLogging {

  import JsonConversion._

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val routes: Route = {
    path("photos" ~ Slash.?) {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          optionalHeaderValue(extractUserToken) { user ⇒
            user
              .map { userResponse ⇒
                val res = for {
                  u ← userResponse
                  photos ← photoService.list(pageNumber, limit, userService.publishedFlag(u))
                } yield photos

                onSuccess(res) {
                  case Right(pageOfPhotos) ⇒ complete(pageOfPhotos)
                  case Left(e) ⇒ completeWithError(e)
                }
              }
              .getOrElse {
                onSuccess(photoService.list(pageNumber, limit, Some(true))) {
                  case Right(pageOfPhotos) ⇒ complete(pageOfPhotos)
                  case Left(e) ⇒ completeWithError(e)
                }
              }

          }
        }
      }
    } ~ path("photos" / LongNumber) { id ⇒
      optionalHeaderValue(extractUserToken) { userResponse ⇒
        userResponse
          .map { userFuture ⇒
            onSuccess(photoService.photoById(id)) {
              case Right(photo) ⇒
                userFuture.map {
                  case Right(u) if !photo.published && u.isSuperuser ⇒
                    logger.info(s"${u.firstName} ${u.lastName} requested album id: $id")
                    complete(photo)
                  case Right(u) if !photo.published ⇒
                    logger.info(s"${u.firstName} ${u.lastName} requested album id: $id")
                    completeWithError(AuthenticationError(
                      "This album is not published you need to be an admin to see it"))
                  case Left(ex) if !photo.published ⇒
                    logger.info(s"Failed to retrieve user with token ${ex.msg}")
                    completeWithError(AuthenticationError(
                      "This album is not published you need to be authenticated to see it"))
                  case Left(ex) ⇒
                    logger.info(s"Failed to retrieve user with token ${ex.msg}")
                }
                complete(photo)
              case Left(e) ⇒ completeWithError(e)
            }
          }
          .getOrElse {
            onSuccess(photoService.photoById(id)) {
              case Right(photo) ⇒ complete(photo)
              case Left(error) ⇒ completeWithError(error)
            }
          }
      }

    } ~ path("photos" / "album" / LongNumber) { id =>
      {
        get {
          parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
            optionalHeaderValue(extractUserToken) { userResponse ⇒
              userResponse
                .map { userFuture ⇒
                  val res = for {
                    u ← userFuture
                    photos ← photoService.photosByAlbum(
                      id,
                      pageNumber,
                      limit,
                      userService.publishedFlag(u))
                  } yield photos

                  onSuccess(res) {
                    case Right(page) ⇒ complete(page)
                    case Left(e) ⇒ completeWithError(e)
                  }
                }
                .getOrElse {
                  onSuccess(photoService.photosByAlbum(id, pageNumber, limit, None)) {
                    case Right(photo) ⇒ complete(photo)
                    case Left(error) ⇒ completeWithError(error)
                  }
                }
            }
          }
        }
      }
    }
  }
}
