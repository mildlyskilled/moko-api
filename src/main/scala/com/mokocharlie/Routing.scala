package com.mokocharlie

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.mokocharlie.model.Photo
import com.mokocharlie.repository.PhotoRepository

import scala.concurrent.{ExecutionContext, Future}


trait Routing extends PhotoRepository with Marshalling {
  implicit def ec: ExecutionContext

  var routes: Route =
    path("photos") {
      get {
        parameters('page.as[Int], 'limit.as[Int])
        val photosFuture = PhotoDAO.list(1, 10)
        onSuccess(photosFuture) {
          case page => complete(page)
        }
      }
    } ~
      pathPrefix("photo" / LongNumber) { id =>
        val photoFuture: Future[Option[Photo]] = PhotoDAO.findPhotoByID(id)
        onSuccess(photoFuture) {
          case Some(photo) => complete(photo)
          case None => complete(StatusCode.int2StatusCode(404))
        }
      }

}
