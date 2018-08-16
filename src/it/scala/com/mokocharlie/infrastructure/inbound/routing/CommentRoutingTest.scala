package com.mokocharlie.infrastructure.inbound.routing

import java.time.LocalDateTime

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Comment
import com.mokocharlie.domain.common.RequestEntity.CommentRequest
import com.mokocharlie.domain.{Page, Token}
import com.mokocharlie.domain.common.{RequestEntity, SettableClock}
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.db.{
  DBCommentRepository,
  DBPhotoRepository,
  DBTokenRepository,
  DBUserRepository
}
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service.{CommentService, PhotoService, UserService}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.duration._

class CommentRoutingTest
    extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with JsonConversion {

  val config: Config = ConfigFactory.load()

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  private val commentRepository = new DBCommentRepository(config)
  private val commentService = new CommentService(commentRepository)
  private val userRepository = new DBUserRepository(config)
  private val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  private val tokenRepository = new DBTokenRepository(config, clock)
  private val photoRepository = new DBPhotoRepository(config)
  private val photoService = new PhotoService(photoRepository, commentRepository)
  private val userService: UserService =
    new UserService(
      userRepository,
      tokenRepository,
      new BearerTokenGenerator,
      clock,
      config.getInt("mokocharlie.auth.token.ttl-in-days"))
  val commentRouting: Route =
    new CommentRouting(commentService, userService, photoService, clock).routes
  val userRoutes: Route = new UserRouting(userService).routes

  var token = "";

  "Comment Route" should "retrieve most recent comments" in {
    Get("/comments") ~> commentRouting ~> check {
      val comments = responseAs[String].parseJson.convertTo[Page[Comment]]
      comments.items should have size 10
      comments.items.forall(_.approved) shouldBe true
      comments.total shouldBe 1775
    }
  }

  it should "retrieve comments on a photo" in {
    Get("/comments/photo/1974") ~> commentRouting ~> check {
      val comments = responseAs[String].parseJson.convertTo[Page[Comment]]
      comments.items.forall(_.photo.id == 1974) shouldBe true
      comments.items.forall(_.approved) shouldBe true
      comments.total shouldBe 50
    }
  }

  it should "retrieve comments on an album" in {
    Get("/comments/album/126") ~> commentRouting ~> check {
      val comments = responseAs[String].parseJson.convertTo[Page[Comment]]
      comments.total shouldBe 133
      println(comments.items.filterNot(_.approved).map(_.id))
      comments.items.forall(_.approved) shouldBe true
    }
  }

  it should "retrieve a comment with a given ID" in {
    Get("/comments/123") ~> commentRouting ~> check {
      val comment = responseAs[String].parseJson.convertTo[Comment]
      comment.id shouldBe 123
    }
  }

  it should "post comments" in {
    val authRequest = RequestEntity.AuthRequest("contact@kaning.co.uk", "dayl1ght")
    val authEntity = HttpEntity(MediaTypes.`application/json`, authRequest.toJson.toString)
    Post("/auth").withEntity(authEntity) ~> userRoutes ~> check {
      val receivedToken = responseAs[String].parseJson.convertTo[Token]
      response.status shouldBe StatusCodes.OK
      token = receivedToken.value
    }
    val header = RawHeader("X-MOKO-USER-TOKEN", token)
    val commentRequest = RequestEntity.CommentRequest(
      userId = 1,
      photoId = 1974,
      comment = "Integration test",
      author = "Kwabena Aning")
    val commentEntity = HttpEntity(MediaTypes.`application/json`, commentRequest.toJson.toString)
    Post("/comments").withEntity(commentEntity).addHeader(header) ~> commentRouting ~> check {
      println(responseAs[String])
    }
  }
}
