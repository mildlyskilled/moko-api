package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.VideoRepository

class VideoRouting(repo: VideoRepository) extends SprayJsonSupport {

  import JsonConversion._

  def routes: Route = {
    path("videos") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
          val albumsFuture = repo.list(pageNumber, limit)
          onSuccess(albumsFuture)(page => complete(page))
        }
      }
    } ~ path("videos" / LongNumber) { id =>
      get {
        val videoFuture = repo.findVideoByID(id)
        onSuccess(videoFuture) {
          case Some(video) => complete(video)
          case None        => complete(StatusCodes.NotFound)
        }
      }
    }
  }
}
