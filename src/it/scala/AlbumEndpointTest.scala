import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.inbound.routing.AlbumRouting
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, DBCommentRepository, DBPhotoRepository}
import com.mokocharlie.service.{AlbumService, PhotoService}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.duration._

class AlbumEndpointTest extends FlatSpec with ScalatestRouteTest with Matchers with JsonConversion {

  implicit val timeout  = RouteTestTimeout(5.seconds dilated)

  val config: Config = ConfigFactory.load()
  val photoRepo = new DBPhotoRepository(config)
  val commentRepo = new DBCommentRepository(config)
  val photoService = new PhotoService(photoRepo, commentRepo)
  val albumRepository = new DBAlbumRepository(config, photoRepo)
  val albumService = new AlbumService(albumRepository, photoService)
  val albumRoute: Route = new AlbumRouting(albumService).routes


  "Album route" should "return a list of albums" in {
    Get("/albums") ~> albumRoute ~> check {
      println(responseAs[String])
      val albumPage = responseAs[String].parseJson.convertTo[Page[Album]]
      albumPage.items should have size 10
    }
  }
}
