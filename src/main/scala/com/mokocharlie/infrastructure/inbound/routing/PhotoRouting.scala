package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.service.PhotoService

class PhotoRouting(photoService: PhotoService)
  extends SprayJsonSupport
    with JsonConversion {

  var routes: Route = {
    path("photos") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) ⇒ {
            onSuccess(photoService.list(pageNumber, limit)) {
              case Right(pageOfPhotos) ⇒ complete(pageOfPhotos)
              case Left(ex) ⇒ complete(StatusCodes.InternalServerError, ex.msg)
            }
          }
        }

      }
    } ~ path("photos" / LongNumber) { id =>
      onSuccess(photoService.photoById(id)) {
        case Right(photo) ⇒ complete(photo)
        case Left(e) ⇒ complete(StatusCodes.NotFound, e.msg)
      }
    } ~ path("photos" / LongNumber / "comments") { id =>
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val commentsFuture = photoService.commentsByPhotoId(id, pageNumber, limit)
            onSuccess(commentsFuture) {
              case Right(page) ⇒ complete(page)
              case Left(e) ⇒ complete(e.msg)
            }
          }
        }
      }
    } ~ path("photos" / "album" / LongNumber) { id => {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            onSuccess(photoService.photosByAlbum(id, pageNumber, limit)) {
              case Right(page) ⇒ complete(page)
              case Left(e) ⇒ complete(StatusCodes.InternalServerError, e.msg)
            }
          }
        }
      }
    }
    }
  }
}
