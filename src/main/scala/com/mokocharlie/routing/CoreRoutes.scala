package com.mokocharlie.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteConcatenation

object CoreRoutes extends RouteConcatenation {
  val routes = {
    path("") {
      get {
        complete("Mokocharlie API")
      }
    }
  } ~ {
    FavouriteRouting.routes
  } ~ {
    CommentRouting.routes
  } ~ {
    PhotoRouting.routes
  } ~ {
    AlbumRouting.routes
  } ~ {
    UserRouting.routes
  } ~ {
    CollectionRouting.routes
  }
}
