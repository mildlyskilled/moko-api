package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.AlbumService
import scala.collection.immutable.Seq

class AlbumRouting(albumService: AlbumService) extends SprayJsonSupport with HttpUtils {

  import JsonConversion._

  val routes: Route = {
    path("albums") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          onSuccess(albumService.list(pageNumber, limit, Seq.empty)) {
            case Right(albumPage) ⇒ complete(albumPage)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
    } ~ path("albums" / LongNumber) { id =>
      onSuccess(albumService.albumById(id)) {
        case Right(album) => complete(album)
        case Left(error) => completeWithError(error)
      }
    } ~ path("albums" / "featured") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) ⇒ {
            onSuccess(albumService.featuredAlbums(pageNumber, limit)){
              case Right(page) ⇒ complete(page)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }
    }
  }
}
