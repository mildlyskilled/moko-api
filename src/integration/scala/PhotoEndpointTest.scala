import java.time.{Clock, LocalDateTime}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.scalatest._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mokocharlie.infrastructure.repository.{DBCommentRepository, DBPhotoRepository, DBTokenRepository, DBUserRepository}
import com.mokocharlie.service.{CommentService, PhotoService, UserService}
import com.typesafe.config.ConfigFactory
import com.mokocharlie.SettableClock
import com.mokocharlie.infrastructure.inbound.routing.PhotoRouting
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import scala.collection.immutable.Seq

class PhotoEndpointTest extends WordSpec with Matchers with ScalatestRouteTest {

  implicit val ec = system.dispatcher
  private val config = ConfigFactory.load()
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
  "Photo Route" should {
    "Return a list of images" in {
      Get("/photos").withHeaders(Seq(tokenHeaders)) ~> photoRoute ~> check {
        responseAs[String] shouldBe "PONG!"
      }
    }
  }

}
