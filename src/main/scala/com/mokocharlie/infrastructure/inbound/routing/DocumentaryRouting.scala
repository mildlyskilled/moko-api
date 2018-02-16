package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.DocumentaryRepository

class DocumentaryRouting(repo: DocumentaryRepository) extends SprayJsonSupport {

  import JsonConversion._

  def routes: Route = {
    path("documentaries") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
          val documentaryFuture = repo.list(pageNumber, limit)
          onSuccess(documentaryFuture)(page => complete(page))
        }
      }
    } ~ path("documentaries" / LongNumber) { id =>
      get {
        val documentaryFuture = repo.findDocumentaryByID(id)
        onSuccess(documentaryFuture) {
          case Some(documentary) => complete(documentary)
          case None              => complete(StatusCodes.NotFound)
        }
      }
    }
  }
}
