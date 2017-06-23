package com.mokocharlie.infrastructure.inbound

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mokocharlie.infrastructure.outbound.Marshalling
import com.mokocharlie.infrastructure.repository.FavouriteRepository

object FavouriteRouting extends FavouriteRepository with Marshalling {
  val routes: Route = cors() {
    path("photos" / LongNumber / "favourites") { id =>
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val favouriteFuture = FavouriteDAO.findFavouritesByImageID(id, pageNumber, limit)
            onSuccess(favouriteFuture)(favSeq => complete(favSeq))
          }
        }
      }
    }
  }
}
