package com.mokocharlie.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import ch.megard.akka.http.cors.CorsDirectives._
import com.mokocharlie.Marshalling
import com.mokocharlie.repository.FavouriteRepository

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
