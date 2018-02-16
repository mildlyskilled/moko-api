package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, DBCommentRepository, DBPhotoRepository}
import com.mokocharlie.service.{AlbumService, PhotoService}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.duration._

class AlbumRoutingTest extends FlatSpec with ScalatestRouteTest with Matchers with JsonConversion {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  val config: Config = ConfigFactory.load()
  val photoRepo = new DBPhotoRepository(config)
  val commentRepo = new DBCommentRepository(config)
  val photoService = new PhotoService(photoRepo, commentRepo)
  val albumRepository = new DBAlbumRepository(config, photoRepo)
  val albumService = new AlbumService(albumRepository, photoService)
  val albumRoute: Route = new AlbumRouting(albumService).routes

  "Album route" should "return a list of albums" in {
    Get("/albums") ~> albumRoute ~> check {
      val albumPage = responseAs[String].parseJson.convertTo[Page[Album]]
      albumPage.items should have size 10
    }
  }

  it should "return page two of albums" in {
    Get("/albums?page=2&limit=10") ~> albumRoute ~> check {
      val albumPage = responseAs[String].parseJson.convertTo[Page[Album]]
      albumPage.page shouldBe 2
      albumPage.items should have size 10
      albumPage.items.head.id shouldBe 193
    }
  }

  it should "return a given album id 23" in {
    Get("/albums/23") ~> albumRoute ~> check {
      responseAs[String].parseJson.convertTo[Album].id shouldBe 23
    }
  }

  it should "return only featured albums" in {
    Get("/albums/featured") ~> albumRoute ~> check {
      val featuredAlbumPage = responseAs[String].parseJson.convertTo[Page[Album]]
      featuredAlbumPage.items should have size 10
      featuredAlbumPage.items.forall(_.featured) shouldBe true
    }
  }
}
