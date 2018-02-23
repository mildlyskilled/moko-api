package com.mokocharlie.infrastructure.inbound.routing

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.mokocharlie.infrastructure.repository._
import com.mokocharlie.infrastructure.repository.db._
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service._
import com.typesafe.config.Config

class CoreRoutes(config: Config, clock: Clock)(implicit system: ActorSystem)
    extends RouteConcatenation {
  private val photoRepository = new DBPhotoRepository(config)
  private val commentRepository = new DBCommentRepository(config)
  private val favouriteRepository = new DBFavouriteRepository(config)
  private val favouriteService = new FavouriteService(favouriteRepository, clock)
  private val albumRepository = new DBAlbumRepository(config, photoRepository)
  private val userRepository = new DBUserRepository(config)
  private val tokenRepository = new DBTokenRepository(config)
  private val userService =
    new UserService(userRepository, tokenRepository, new BearerTokenGenerator, clock)
  private val collectionRepository = new DBCollectionRepository(config)
  private val videoRepository = new VideoRepository{} // todo
  private val documentaryRepository = new DocumentaryRepository{} //todo
  private val photoService = new PhotoService(photoRepository, commentRepository)
  private val albumService = new AlbumService(albumRepository, photoService)
  private val collectionService = new CollectionService(collectionRepository)
  private val commentService = new CommentService(commentRepository)
  private val storyRepository = new DBStoryRepository(config)
  private val storyService = new StoryService(storyRepository)

  val routes: Route = {
    path("") {
      get {
        complete("Moko Charlie API")
      }
    }
  } ~ {
    new FavouriteRouting(favouriteService).routes
  } ~ {
    new CommentRouting(commentService, userService).routes
  } ~ {
    new PhotoRouting(photoService, commentService, clock, userService).routes
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
  } ~ {
    new StoryRouting(storyService, userService).routes
  }
}
