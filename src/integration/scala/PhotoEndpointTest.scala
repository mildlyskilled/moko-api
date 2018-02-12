import java.time.LocalDateTime

import akka.http.scaladsl.model.headers.RawHeader
import org.scalatest._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mokocharlie.infrastructure.repository.{
  DBCommentRepository,
  DBPhotoRepository,
  DBTokenRepository,
  DBUserRepository
}
import com.mokocharlie.service.{CommentService, PhotoService, UserService}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import com.mokocharlie.SettableClock
import com.mokocharlie.domain.MokoModel.Photo
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.inbound.routing.PhotoRouting
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.BearerTokenGenerator

import spray.json._

class PhotoEndpointTest extends FlatSpec with Matchers with ScalatestRouteTest with JsonConversion {

  implicit val ec = system.dispatcher
  private val config = ConfigFactory.load()
  config.withValue("dbName", ConfigValueFactory.fromAnyRef("mokocharlie"))

  private val photoRepository = new DBPhotoRepository(config)
  private val commentRepo = new DBCommentRepository(config)
  private val commentService = new CommentService(commentRepo)
  private val userRepository = new DBUserRepository(config)
  private val tokenRepository = new DBTokenRepository(config)
  private val photosService = new PhotoService(photoRepository, commentRepo)
  private val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  implicit val userService: UserService =
    new UserService(userRepository, tokenRepository, new BearerTokenGenerator, clock)
  private val photoRoute =
    new PhotoRouting(photosService, commentService, clock, userService).routes

  val tokenHeaders = RawHeader("X-MOKO-USER", "sometoken thatgoesnowhere")

  "Photo Route" should "Return a list of images" in {
      Get("/photos") ~> photoRoute ~> check {
        val page = responseAs[String].parseJson.convertTo[Page[Photo]]
        page.items should have size 10
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
