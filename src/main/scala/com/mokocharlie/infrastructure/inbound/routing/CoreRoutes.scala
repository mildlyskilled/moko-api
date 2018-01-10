package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.mokocharlie.infrastructure.repository._
import com.mokocharlie.infrastructure.service.{AlbumService, CommentService, PhotoService}
import com.typesafe.config.Config

class CoreRoutes(config: Config)(implicit system: ActorSystem) extends RouteConcatenation {
  private val photoRepository = new PhotoRepository(config)
  private val commentRepository = new CommentRepository(config)
  private val favouriteRepository = new FavouriteRepository(config)
  private val albumRepository = new AlbumRepository(config)
  private val userRepository = new UserRepository(config)
  private val collectionRepository = new CollectionRepository(config)
  private val videoRepository = new VideoRepository(config)
  private val documentaryRepository = new DocumentaryRepository(config)

  val routes: Route = {
    path("") {
      get {
        complete("Moko Charlie API")
      }
    }
  } ~ {
    new FavouriteRouting(favouriteRepository).routes
  } ~ {
    new CommentRouting(new CommentService(commentRepository)).routes
  } ~ {
    val photoService = new PhotoService(photoRepository, commentRepository)
    new PhotoRouting(photoService).routes
  } ~ {
    val albumService = new AlbumService(albumRepository, photoRepository)
    new AlbumRouting(albumService).routes
  } ~ {
    new UserRouting(userRepository).routes
  } ~ {
    new CollectionRouting(collectionRepository).routes
  } ~ {
    new VideoRouting(videoRepository).routes
  } ~ {
    new DocumentaryRouting(documentaryRepository).routes
  }
}
