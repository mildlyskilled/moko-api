package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.FavouriteRepository

class FavouriteRouting(repo: FavouriteRepository)
  extends SprayJsonSupport
    with JsonConversion {
  val routes: Route = {
    path("photos" / LongNumber / "favourites") { id =>
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val favouriteFuture = repo.findFavouritesByImageID(id, pageNumber, limit)
            onSuccess(favouriteFuture)(favSeq => complete(favSeq))
          }
        }
      }
    }
  }
}
