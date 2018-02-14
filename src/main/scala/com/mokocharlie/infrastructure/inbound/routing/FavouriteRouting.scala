package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.domain.common.RequestEntity.FavouriteRequest
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.FavouriteService
import com.typesafe.scalalogging.StrictLogging

class FavouriteRouting(service: FavouriteService)
    extends SprayJsonSupport
    with HttpUtils
    with JsonConversion
    with StrictLogging {
  val routes: Route = {
    path("favourites" / LongNumber) { id =>
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
          {
            onSuccess(service.imageFavourites(id, pageNumber, limit)) {
              case Right(faves) ⇒ complete(faves)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }
    } ~ path("favourites" ~ Slash.?) {
      put {
        entity(as[FavouriteRequest]) { favourite ⇒
          onSuccess(service.addFavourite(favourite.userId, favourite.photoId)) {
            case Right(id) ⇒ complete(StatusCodes.Accepted, s"Favourite created with id: $id")
            case Left(error) ⇒ completeWithError(error)
          }

        }
      }
    }
  }
}
