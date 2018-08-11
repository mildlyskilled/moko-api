package com.mokocharlie.infrastructure.inbound.routing

import java.time.LocalDateTime

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.SettableClock
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.ITFakeTokenRepository
import com.mokocharlie.infrastructure.repository.db.{
  DBAlbumRepository,
  DBCommentRepository,
  DBPhotoRepository,
  DBUserRepository
}
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service.{AlbumService, PhotoService, UserService}
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
  private val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  private val userRepository = new DBUserRepository(config)
  private val tokenRepository = new ITFakeTokenRepository(config, clock)
  implicit val userService: UserService =
    new UserService(
      userRepository,
      tokenRepository,
      new BearerTokenGenerator,
      clock,
      config.getInt("mokocharlie.auth.token.ttl-in-days"))
  val albumRoute: Route = new AlbumRouting(albumService, userService).routes

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
      albumPage.items.head.id shouldBe Some(193)
    }
  }

  it should "return a given album id 23" in {
    Get("/albums/23") ~> albumRoute ~> check {
      responseAs[String].parseJson.convertTo[Album].id shouldBe Some(23)
    }
  }

  it should "return only featured albums" in {
    Get("/albums/featured") ~> albumRoute ~> check {
      val featuredAlbumPage = responseAs[String].parseJson.convertTo[Page[Album]]
      featuredAlbumPage.items should have size 6
      featuredAlbumPage.items.forall(_.featured) shouldBe true
    }
  }
}
