package com.mokocharlie.routing

import akka.http.scaladsl.server.RouteConcatenation

object CoreRoutes extends RouteConcatenation {
  val routes = PhotoRouting.routes ~ AlbumRouting.routes
}
