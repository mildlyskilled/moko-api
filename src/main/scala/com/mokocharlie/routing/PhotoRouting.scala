package com.mokocharlie.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.Marshalling
import com.mokocharlie.model.Photo
import com.mokocharlie.repository.PhotoRepository

import scala.concurrent.Future


object PhotoRouting extends PhotoRepository with Marshalling {

  var routes: Route = {
    path("photos") {
      get {
        parameters('page.as[Int], 'limit.as[Int]) {
          (pageNumber, limit) => {
            val photosFuture = PhotoDAO.list(pageNumber, limit)
            onSuccess(photosFuture) {
              case page => complete(page)
            }
          }
        }

      }
    } ~
      pathPrefix("photos" / LongNumber) { id =>
        val photoFuture: Future[Option[Photo]] = PhotoDAO.findPhotoByID(id)
        onSuccess(photoFuture) {
          case Some(photo) => complete(photo)
          case None => complete(StatusCode.int2StatusCode(404))
        }
      }

  }
}
