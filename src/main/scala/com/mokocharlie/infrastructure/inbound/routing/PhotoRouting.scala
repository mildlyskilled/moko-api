package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.common.MokoCharlieServiceError.OperationDisallowed
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.spartan.HotGate
import com.mokocharlie.service.{CommentService, PhotoService, UserService}

import scala.concurrent.Future

class PhotoRouting(
    photoService: PhotoService,
    commentService: CommentService,
    override val userService: UserService)(implicit system: ActorSystem)
    extends SprayJsonSupport
    with JsonConversion
    with HotGate
    with HttpErrorMapper {

  implicit val ec = system.dispatcher

  var routes: Route = {
    path("photos" / LongNumber) { id =>
      onSuccess(photoService.photoById(id)) {
        case Right(photo) ⇒ complete(photo)
        case Left(e) ⇒ completeWithError(e)
      }
    } ~ path("photos") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          extractUser { user ⇒
            val res = for {
              u ← user
              f ← photoService.list(pageNumber, limit, u.map(_.isSuperuser).toOption)
            } yield f

            onSuccess(res) {
              case Right(pageOfPhotos) ⇒ complete(pageOfPhotos)
              case Left(e) ⇒ completeWithError(e)
            }
          }
        }

      }
    } ~ path("photos" / "album" / LongNumber) { id =>
      {
        get {
          parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
            {
              onSuccess(photoService.photosByAlbum(id, pageNumber, limit)) {
                case Right(page) ⇒ complete(page)
                case Left(e) ⇒ completeWithError(e)
              }
            }
          }
        }
      }
    }
  }
}
