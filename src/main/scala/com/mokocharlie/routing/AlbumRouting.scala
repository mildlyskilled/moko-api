package com.mokocharlie.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.Marshalling
import com.mokocharlie.model.Album
import com.mokocharlie.repository.AlbumRepository

import scala.concurrent.Future

object AlbumRouting extends AlbumRepository with Marshalling {

  val routes: Route = {
    path("albums") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => val albumsFuture = AlbumDAO.list(pageNumber, limit)
            onSuccess(albumsFuture) {
              case page => complete(page)
            }
        }
      }
    } ~
      pathPrefix("albums" / LongNumber) { id =>
        val albumFuture: Future[Option[Album]] = AlbumDAO.findAlbumByID(id)
        onSuccess(albumFuture) {
          case Some(album) => complete(album)
          case None => complete(StatusCode.int2StatusCode(404))
        }
      }
  }

}
