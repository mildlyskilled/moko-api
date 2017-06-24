package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.DocumentaryRepository

object DocumentaryRouting
  extends DocumentaryRepository
    with SprayJsonSupport
    with JsonConversion {

  def routes: Route = cors() {
    path("documentaries") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) =>
            val documentaryFuture = DocumentaryDAO.list(pageNumber, limit)
            onSuccess(documentaryFuture)(page => complete(page))
        }
      }
    } ~ path("documentaries" / LongNumber) { id =>
      get {
        val documentaryFuture = DocumentaryDAO.findDocumentaryByID(id)
        onSuccess(documentaryFuture) {
          case Some(documentary) => complete(documentary)
          case None => complete(StatusCodes.NotFound)
        }
      }
    }
  }
}
