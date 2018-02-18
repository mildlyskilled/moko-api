package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.{AlbumService, CollectionService}

class CollectionRouting(service: CollectionService, albumService: AlbumService)
    extends SprayJsonSupport
    with HttpUtils {

  import JsonConversion._

  val routes: Route = {
    path("collections" ~ Slash.?) {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) =>
          {
            onSuccess(service.list(page, limit)) {
              case Right(collectionPage) ⇒ complete(collectionPage)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }
    } ~ path("collections" / "featured" ~ Slash.?) {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) ⇒
          {
            onSuccess(service.featuredCollection(page, limit)) {
              case Right(collectionPage) ⇒ complete(collectionPage)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }
    } ~ path("collections" / LongNumber) { id =>
      onSuccess(service.collectionById(id)) {
        case Right(collection) ⇒ complete(collection)
        case Left(error) ⇒ completeWithError(error)
      }
    } ~ path("collections" / LongNumber / "albums") { id =>
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) =>
        {
          onSuccess(albumService.collectionAlbums(id, page, limit)) {
            case Right(albumPage) ⇒ complete(albumPage)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
    }
  }
}
