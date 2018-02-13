
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.inbound.routing.AlbumRouting
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, DBCommentRepository, DBPhotoRepository}
import com.mokocharlie.service.{AlbumService, PhotoService}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class AlbumEndpointTest extends FlatSpec with ScalatestRouteTest with Matchers {

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
}
