package com.mokocharlie.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.CorsDirectives._
import com.mokocharlie.Marshalling
import com.mokocharlie.model.{Page, Photo}
import com.mokocharlie.repository.{CommentRepository, PhotoRepository}

import scala.concurrent.Future


object PhotoRouting extends PhotoRepository with CommentRepository with Marshalling {

  var routes: Route = cors() {
    path("photos") {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val photosFuture = PhotoDAO.list(pageNumber, limit)
            onSuccess(photosFuture)(page => complete(page))
          }
        }

      }
    } ~ path("photos" / LongNumber) { id =>
      val photoFuture: Future[Option[Photo]] = PhotoDAO.findPhotoByID(id)
      onSuccess(photoFuture) {
        case Some(photo) => complete(photo)
        case None => complete(StatusCodes.NotFound)
      }
    } ~ path("photos" / LongNumber / "comments") { id =>
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val commentsFuture = CommentDAO.findCommentsByImageID(id, pageNumber, limit)
            onSuccess(commentsFuture)(page => complete(page))
          }
        }
      }
    } ~ path("photos" / "album" / LongNumber) { id => {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) {
          (pageNumber, limit) => {
            val albumPhotos: Future[Page[Photo]] = PhotoDAO.getPhotosByAlbumId(id, pageNumber, limit)
            onSuccess(albumPhotos)(page => complete(page))
          }
        }
      }
    }
    }
  }
}
