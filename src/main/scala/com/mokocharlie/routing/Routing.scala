package com.mokocharlie.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.mokocharlie.model.{Page, Photo}
import com.mokocharlie.repository.PhotoRepository
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future


trait Routing extends PhotoRepository {

  val photoRepository = PhotoDAO

  // formats for unmarshalling and marshalling
  implicit val photoFormat = jsonFormat11(Photo)

  // needed to run the route
  implicit val materializer = ActorMaterializer()

  var route: Route =
    get {
      pathPrefix("photo" / LongNumber) { id =>
        val photoFuture:Future[Option[Photo]] = PhotoDAO.findPhotoByID(id)
        onSuccess(photoFuture) {
          case Some(photo) => complete(photo)
          case None => complete(StatusCode.NotFound)
        }
      }
    }

}
