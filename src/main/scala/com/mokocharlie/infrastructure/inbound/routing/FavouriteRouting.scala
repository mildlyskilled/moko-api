package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.FavouriteService

class FavouriteRouting(service: FavouriteService)
    extends SprayJsonSupport
    with HttpErrorMapper
    with JsonConversion {
  val routes: Route = {
    path("photos" / LongNumber / "favourites") { id =>
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
    }
  }
}
