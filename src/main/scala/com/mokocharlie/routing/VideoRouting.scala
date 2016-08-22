package com.mokocharlie.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.CorsDirectives._
import com.mokocharlie.Marshalling
import com.mokocharlie.repository.VideoRepository

object VideoRouting extends VideoRepository with Marshalling {

  def routes: Route = cors() {
    path("videos") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => val albumsFuture = VideoDAO.list(pageNumber, limit)
            onSuccess(albumsFuture)(page => complete(page))
        }
      }
    } ~ path("videos" / LongNumber) { id =>
      get {
        val videoFuture = VideoDAO.findVideoByID(id)
        onSuccess(videoFuture) {
          case Some(video) => complete(video)
          case None => complete(StatusCodes.NotFound)
        }
      }
    }
  }
}
