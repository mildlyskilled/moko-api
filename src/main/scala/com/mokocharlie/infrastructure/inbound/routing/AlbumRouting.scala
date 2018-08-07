package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.common.MokoCharlieServiceError.AuthenticationError
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.HeaderChecking
import com.mokocharlie.service.{AlbumService, UserService}

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext

class AlbumRouting(albumService: AlbumService, override val userService: UserService)(
    implicit system: ActorSystem)
    extends SprayJsonSupport
    with HttpUtils
    with HeaderChecking {
  implicit val ec: ExecutionContext = system.dispatcher
  import JsonConversion._

  val routes: Route = {
    path("albums") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          optionalHeaderValue(extractUserToken) { user ⇒
            user
              .map { userResponse ⇒
                val res = for {
                  u ← userResponse
                  albums ← albumService.list(
                    pageNumber,
                    limit,
                    Seq.empty,
                    userService.publishedFlag(u))
                } yield albums

                onSuccess(res) {
                  case Right(albumPage) ⇒ complete(albumPage)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
              .getOrElse {
                onSuccess(albumService.list(pageNumber, limit, Seq.empty, Some(true))) {
                  case Right(albumPage) ⇒ complete(albumPage)
                  case Left(exc) ⇒ completeWithError(exc)
                }
              }
          }
        }
      }
    } ~ path("albums" / LongNumber) { id =>
      optionalHeaderValue(extractUserToken) { userResponse ⇒
        userResponse
          .map { userFuture ⇒
            onSuccess(albumService.albumById(id)) {
              case Right(album) ⇒
                userFuture.map {
                  case Right(u) if !album.published && u.isSuperuser ⇒
                    logger.info(s"${u.firstName} ${u.lastName} requested photo id: $id")
                    complete(album)
                  case Right(u) if !album.published ⇒
                    logger.info(s"${u.firstName} ${u.lastName} requested photo id: $id")
                    completeWithError(AuthenticationError(
                      "This image is not published you need to be an admin to see it"))
                  case Left(ex) if !album.published ⇒
                    logger.info(s"Failed to retrieve user with token ${ex.msg}")
                    completeWithError(AuthenticationError(
                      "This image is not published you need to be authenticated to see it"))
                  case Left(ex) ⇒
                    logger.info(s"Failed to retrieve user with token ${ex.msg}")
                }
                complete(album)
              case Left(e) ⇒ completeWithError(e)
            }
          }.getOrElse{
          onSuccess(albumService.albumById(id)) {
            case Right(album) => complete(album)
            case Left(error)  => completeWithError(error)
          }
        }
      }

    } ~ path("albums" / "featured") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          {
            onSuccess(albumService.featuredAlbums(pageNumber, limit)) {
              case Right(page) ⇒ complete(page)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }
    }
  }
}
