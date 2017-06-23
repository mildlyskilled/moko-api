package com.mokocharlie.infrastructure.inbound

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mokocharlie.domain.Album
import com.mokocharlie.infrastructure.outbound.Marshalling
import com.mokocharlie.infrastructure.repository.{AlbumRepository, PhotoRepository}

import scala.concurrent.Future

object AlbumRouting extends AlbumRepository with PhotoRepository with Marshalling {

  val routes: Route = cors() {
    path("albums") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => val albumsFuture = AlbumDAO.list(pageNumber, limit)
            onSuccess(albumsFuture)(page => complete(page))
        }
      }
    } ~ path("albums" / LongNumber) { id =>
      val albumFuture: Future[Option[Album]] = AlbumDAO.findAlbumByID(id)
      onSuccess(albumFuture) {
        case Some(album) => complete(album)
        case None => complete(StatusCode.int2StatusCode(404))
      }
    } ~ path("albums" / "featured") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val featuredAlbumsFuture = AlbumDAO.getFeaturedAlbums(pageNumber, limit)
            onSuccess(featuredAlbumsFuture)(page => complete(page))
          }
        }
      }
    }
  }
}
