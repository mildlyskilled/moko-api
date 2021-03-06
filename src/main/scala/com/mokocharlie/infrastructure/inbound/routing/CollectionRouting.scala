package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.CollectionRepository
import com.mokocharlie.infrastructure.service.{AlbumService, CollectionService}

import scala.concurrent.Future

class CollectionRouting(service: CollectionService, albumService: AlbumService)
  extends SprayJsonSupport
    with JsonConversion
  with HttpErrorMapper {

  val routes: Route = {
    path("collections") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (page, limit) => {
            onSuccess(service.featuredCollection(page, limit)) {
              case Right(collectionPage) ⇒ complete(collectionPage)
              case Left(error) ⇒
                val apiError = toHttpError(error)
                complete(apiError.code, apiError.message)
            }
          }
        }
      }
    } ~ path("collections" / LongNumber) { id =>
      onSuccess(service.collectionById(id)) {
        case Right(collection) ⇒ complete(collection)
        case Left(error) ⇒
          val apiError = toHttpError(error)
          complete(apiError.code, apiError.message)
      }
    } ~ path("collections" / LongNumber / "albums") { id =>
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
        (page, limit) => {
          onSuccess(albumService.collectionAlbums(id, page, limit)){
            case Right(albumPage) ⇒ complete(albumPage)
            case Left(error) ⇒
              val apiError = toHttpError(error)
              complete(apiError.code, apiError.message)
          }
        }
      }

    }
  }

}
