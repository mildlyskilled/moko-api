package com.mokocharlie.infrastructure.inbound

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mokocharlie.domain.{Album, Collection, Page}
import com.mokocharlie.infrastructure.outbound.Marshalling
import com.mokocharlie.infrastructure.repository.CollectionRepository

import scala.concurrent.Future

object CollectionRouting extends CollectionRepository with Marshalling {

  val routes: Route = cors() {
    path("collections") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (page, limit) => {
            val collectionFuture = CollectionDAO.getFeaturedCollections(page, limit)
            onSuccess(collectionFuture)(page => complete(page))
          }
        }
        }
      } ~ path("collections" / LongNumber) { id =>
      val collectionFuture: Future[Option[Collection]] = CollectionDAO.findCollectionById(id)
      onSuccess(collectionFuture) {
        case Some(collection) => complete(collection)
        case None => complete(StatusCodes.NotFound.toString)
      }
    } ~ path("collections" / LongNumber / "albums") { id =>
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
        (page, limit) => {
          val albumsFuture: Future[Page[Album]] = CollectionDAO.getCollectionAlbums(id, page, limit)
          onSuccess(albumsFuture)(page => complete(page))
        }
      }

    }
  }

}
