package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Collection
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository._
import com.mokocharlie.service.{AlbumService, CollectionService, PhotoService}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.duration._

class CollectionRoutingTest
    extends FlatSpec
    with ScalatestRouteTest
    with Matchers
    with JsonConversion {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)
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

  it should "return a featured collection" in {
    Get("/collections/featured") ~> collectionRoute ~> check {
      val collectionPage = responseAs[String].parseJson.convertTo[Page[Collection]]
      collectionPage.items.forall(_.featured) shouldBe true
    }
  }

  it should "return a collection with id 2" in {
    Get("/collections/2") ~> collectionRoute ~> check {
      val collection = responseAs[String].parseJson.convertTo[Collection]
      collection.id shouldBe 2
    }
  }
}
