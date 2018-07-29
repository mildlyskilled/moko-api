package com.mokocharlie.infrastructure.inbound.routing

import java.time.LocalDateTime

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Hospitality
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.SettableClock
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.db._
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service._
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.duration._

class HospitalityRoutingTest
    extends FlatSpec
    with ScalatestRouteTest
    with Matchers
    with JsonConversion {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)
  private val config = ConfigFactory.load()
  private val contactRepo = new DBContactRepository(config)
  private val contactService = new ContactService(contactRepo)
  private val userRepository = new DBUserRepository(config)
  private val photoRepository = new DBPhotoRepository(config)
  private val commentRepository = new DBCommentRepository(config)
  private val photoService = new PhotoService(photoRepository, commentRepository)
  private val albumRepository = new DBAlbumRepository(config, photoRepository)
  private val albumService = new AlbumService(albumRepository, photoService)
  private val tokenRepository = new DBTokenRepository(config)
  private val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  implicit val userService: UserService =
    new UserService(userRepository, tokenRepository, new BearerTokenGenerator, clock)
  private val hospitalityRepo = new DBHospitalityRepository(config, albumRepository)
  val hospitalityService = new HospitalityService(hospitalityRepo, contactService, albumService)

  val hospitalityRoute = new HospitalityRouting(hospitalityService, userService).routes

  "Hospitality route " should "retrieve all hospitality entries" in {
    Get("/hospitality/?page=1&limit=20") ~> hospitalityRoute ~> check {
      val page = responseAs[String].parseJson.convertTo[Page[Hospitality]]
      page should have size 14
    }
  }

  it should "retrieve only resorts" in {
    Get("/hospitality/resort") ~> hospitalityRoute ~> check {
      val page = responseAs[String].parseJson.convertTo[Page[Hospitality]]
      page should have size 3
    }
  }

  it should "retrieve only hotels" in {
    Get("/hospitality/hotel?page1&limit20") ~> hospitalityRoute ~> check {
      val page = responseAs[String].parseJson.convertTo[Page[Hospitality]]
      page should have size 11
    }
  }
  it should "retrieve featured resorts" in {
    Get("/hospitality/featured") ~> hospitalityRoute ~> check {
      val page = responseAs[String].parseJson.convertTo[Page[Hospitality]]
      page.items.forall(_.featured) shouldBe true
    }
  }

}
