package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.domain.common.MokoCharlieServiceError.OperationDisallowed
import com.mokocharlie.domain.common.RequestEntity.FavouriteRequest
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.HeaderChecking
import com.mokocharlie.service.{FavouriteService, UserService}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContextExecutor, Future}

class FavouriteRouting(service: FavouriteService, override val userService: UserService)(
    implicit val system: ActorSystem)
    extends SprayJsonSupport
    with HttpUtils
    with StrictLogging
    with HeaderChecking {

  import JsonConversion._

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val routes: Route = {
    path("favourites" / LongNumber) { id =>
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) =>
          {
            onSuccess(service.imageFavourites(id, pageNumber, limit)) {
              case Right(faves) ⇒ complete(faves)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }
    } ~ path("favourites" ~ Slash.?) {
      put {
        entity(as[FavouriteRequest]) { favourite ⇒
          optionalHeaderValue(extractUserToken) { tokenResponse ⇒
            tokenResponse
              .map { userFuture ⇒
                val res = for {
                  u ← userFuture.user
                  f ← if (u.exists(_.id == favourite.userId))
                    service.addFavourite(favourite.userId, favourite.photoId)
                  else Future.successful(Left(OperationDisallowed("This token was not valid")))
                } yield f

                onSuccess(res) {
                  case Right(id) ⇒ complete(StatusCodes.Accepted, s"Favourite created with id: $id")
                  case Left(error) ⇒ completeWithError(error)
                }
              }
              .getOrElse(completeWithError(OperationDisallowed("This token was not valid")))
          }
        }
      }
    }
  }
}
