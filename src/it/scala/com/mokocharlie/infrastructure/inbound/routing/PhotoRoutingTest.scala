package com.mokocharlie.infrastructure.inbound.routing

import java.time.LocalDateTime

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.mokocharlie.domain.MokoModel.Photo
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.SettableClock
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service.{CommentService, PhotoService, UserService}
import com.typesafe.config.ConfigFactory
import org.scalatest._
import spray.json._
import akka.testkit.TestDuration
import com.mokocharlie.infrastructure.repository.db.{
  DBCommentRepository,
  DBPhotoRepository,
  DBTokenRepository,
  DBUserRepository
}

import scala.concurrent.duration._

class PhotoRoutingTest extends FlatSpec with Matchers with ScalatestRouteTest with JsonConversion {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)
  private val config = ConfigFactory.load()
  private val photoRepository = new DBPhotoRepository(config)
  private val commentRepo = new DBCommentRepository(config)
  private val commentService = new CommentService(commentRepo)
  private val userRepository = new DBUserRepository(config)
  private val photosService = new PhotoService(photoRepository, commentRepo)
  private val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  private val tokenRepository = new DBTokenRepository(config, clock)
  implicit val userService: UserService =
    new UserService(userRepository, tokenRepository, new BearerTokenGenerator, clock)
  private val photoRoute =
    new PhotoRouting(photosService, commentService, clock, userService).routes

  val tokenHeaders = RawHeader("X-MOKO-USER", "sometoken thatgoesnowhere")

  "Photo Route" should "Return a list of images" in {
    Get("/photos") ~> photoRoute ~> check {
      val page = responseAs[String].parseJson.convertTo[Page[Photo]]
      page.items should have size 10
      page.total shouldBe 3295
    }
  }

  it should "return a photo with id: 234" in {
    Get("/photos/234") ~> photoRoute ~> check {
      val photo = responseAs[String].parseJson.convertTo[Photo]
      photo.id shouldBe 234
      photo.name shouldBe "Banku and Tilapia"
    }
  }

  it should "get photos from an album" in {
    Get("/photos/album/30") ~> photoRoute ~> check {
      val photos = responseAs[String].parseJson.convertTo[Page[Photo]]
      photos.items should have size 7
    }
  }
}
