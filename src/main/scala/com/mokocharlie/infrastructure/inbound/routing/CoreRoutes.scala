package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.mokocharlie.infrastructure.repository._
import com.mokocharlie.infrastructure.service._
import com.typesafe.config.Config

class CoreRoutes(config: Config)(implicit system: ActorSystem) extends RouteConcatenation {
  private val photoRepository = new DBPhotoRepository(config)
  private val commentRepository = new DBCommentRepository(config)
  private val favouriteRepository = new FavouriteRepository(config)
  private val albumRepository = new DBAlbumRepository(config, photoRepository)
  private val userRepository = new DBUserRepository(config)
  private val userService = new UserService(userRepository)
  private val collectionRepository = new DBCollectionRepository(config)
  private val videoRepository = new VideoRepository(config)
  private val documentaryRepository = new DocumentaryRepository(config)
  private val photoService = new PhotoService(photoRepository, commentRepository)
  private val albumService = new AlbumService(albumRepository, photoService)
  private val collectionService = new CollectionService(collectionRepository)
  private val commentService = new CommentService(commentRepository)

  val routes: Route = {
    path("") {
      get {
        complete("Moko Charlie API")
      }
    }
  } ~ {
    new FavouriteRouting(favouriteRepository).routes
  } ~ {
    new CommentRouting(commentService).routes
  } ~ {
    new PhotoRouting(photoService, commentService).routes
  } ~ {
    new AlbumRouting(albumService).routes
  } ~ {
    new UserRouting(userService).routes
  } ~ {
    new CollectionRouting(collectionService, albumService).routes
  } ~ {
    new VideoRouting(videoRepository).routes
  } ~ {
    new DocumentaryRouting(documentaryRepository).routes
  }
}
