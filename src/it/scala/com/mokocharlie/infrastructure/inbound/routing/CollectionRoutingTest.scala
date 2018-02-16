package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mokocharlie.domain.MokoModel.Collection
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository._
import com.mokocharlie.service.{AlbumService, CollectionService, PhotoService}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{AsyncFlatSpec, FlatSpec, Matchers}
import spray.json._

class CollectionRoutingTest
    extends FlatSpec
    with ScalatestRouteTest
    with Matchers
    with JsonConversion {
  val config: Config = ConfigFactory.load()
  val photoRepo = new DBPhotoRepository(config)
  val commentRepo = new DBCommentRepository(config)
  val photoService = new PhotoService(photoRepo, commentRepo)
  val albumRepository = new DBAlbumRepository(config, photoRepo)
  val albumService = new AlbumService(albumRepository, photoService)

  val collectionRepo: CollectionRepository = new DBCollectionRepository(config)

  val collectionService = new CollectionService(collectionRepo)
  val collectionRoute: Route = new CollectionRouting(collectionService, albumService).routes

  "Collection route" should "return a list of collections" in {
    Get("/collections") ~> collectionRoute ~> check {
      val collectionPage = responseAs[String].parseJson.convertTo[Page[Collection]]
      collectionPage.items should have size 9
    }
  }
}
