package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.CollectionRepository

import scala.concurrent.Future

class CollectionRouting(repo: CollectionRepository)
  extends SprayJsonSupport
    with JsonConversion {

  val routes: Route = {
    path("collections") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (page, limit) => {
            val collectionFuture = repo.getFeaturedCollections(page, limit)
            onSuccess(collectionFuture)(page => complete(page))
          }
        }
      }
    } ~ path("collections" / LongNumber) { id =>
      val collectionFuture: Future[Option[Collection]] = repo.findCollectionById(id)
      onSuccess(collectionFuture) {
        case Some(collection) => complete(collection)
        case None => complete(StatusCodes.NotFound.toString)
      }
    } ~ path("collections" / LongNumber / "albums") { id =>
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
        (page, limit) => {
          val albumsFuture: Future[Page[Album]] = repo.getCollectionAlbums(id, page, limit)
          onSuccess(albumsFuture)(page => complete(page))
        }
      }

    }
  }

}
