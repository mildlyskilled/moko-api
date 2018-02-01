package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.{CommentService, PhotoService}

class PhotoRouting(photoService: PhotoService, commentService: CommentService)
    extends SprayJsonSupport
    with JsonConversion
    with HttpErrorMapper {

  var routes: Route = {
    path("photos") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          {
            onSuccess(photoService.list(pageNumber, limit)) {
              case Right(pageOfPhotos) ⇒ complete(pageOfPhotos)
              case Left(e) ⇒ completeWithError(e)
            }
          }
        }

      }
    } ~ path("photos" / LongNumber) { id =>
      onSuccess(photoService.photoById(id)) {
        case Right(photo) ⇒ complete(photo)
        case Left(e) ⇒ completeWithError(e)
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
