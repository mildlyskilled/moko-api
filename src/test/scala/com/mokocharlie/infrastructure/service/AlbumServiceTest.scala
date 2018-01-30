package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Photo
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.infrastructure.repository._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContext
import scala.collection.immutable.Seq

@DoNotDiscover
class AlbumServiceTest extends AsyncFlatSpec with Matchers with StrictLogging with TestFixtures {
  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val ec: ExecutionContext = system.dispatcher
  val config: Config = ConfigFactory.load()
  val photoRepository = new DBPhotoRepository(config)
  val albumRepository = new DBAlbumRepository(config, photoRepository)
  val commentRepository = new DBCommentRepository(config)
  val photoService = new PhotoService(photoRepository, commentRepository)
  val albumService = new AlbumService(albumRepository, photoService)

  behavior of "AlbumService"

  logger.info(s"""Running test on
       |${config.getString("mokocharlie.db.host")} with
       |user: ${config.getString("mokocharlie.db.user")} and
       |password: ${config.getString("mokocharlie.db.password")}""".stripMargin)

  "AlbumService" should "create new album with a cover" in {
    albumService
      .createOrUpdate(album1)
      .map {
        case Right(result) ⇒ result shouldBe 1
        case Left(_) ⇒ fail("An album should have been created")
      }
  }

  it should "create an album without a cover" in {
    albumService
      .createOrUpdate(album2)
      .map {
        case Right(result) ⇒ result shouldBe 2
        case Left(ex) ⇒ fail(s"An album should have been created ${ex.msg}")
      }
  }

  it should "eventually return a list of albums" in {
    albumService.list(1, 10).map {
      case Right(albumPage) ⇒ albumPage should have size 2
      case Left(ex) ⇒ fail(s"Album service must return a  successful result {$ex}")
    }
  }

  it should "eventually return a EmptyResultError when given a non-existent id " in {
    albumService.albumById(99999999).map { x ⇒
      x shouldBe Left(EmptyResultSet("Could not find album with given id: 99999999"))
    }
  }

  it should "eventually be updated when values are changed" in {
    albumService.createOrUpdate(album1.copy(label = "Test Update")).flatMap {
      case Right(id) ⇒
        albumService.albumById(id).map {
          case Right(album) ⇒ album.label shouldBe "Test Update"
          case Left(ex) ⇒ fail(s"An album should have been found ${ex.msg}")
        }
      case Left(e) ⇒ fail(s"The update failed $e")
    }
  }

  it should "save photos to a given album" in {
    photoService
      .createOrUpdate(photo2)
      .flatMap {
        case Right(newImageId) ⇒
          albumService.savePhotosToAlbum(album1.id, Seq(photo1.id, newImageId)).flatMap {
            case Right(_) ⇒
              photoService.photosByAlbum(album1.id, 1, 3).map {
                case Right(photos) ⇒ photos.items should contain allOf (photo1, photo2)
                case Left(ex) ⇒ fail(s"A photo should be returned ${ex.msg}")
              }
            case Left(ex) ⇒ fail(s"Photos were not saved ${ex.msg}")
          }
        case Left(ex) ⇒ fail(s"Could not create second image ${ex.msg}")
      }
  }

  it should "remove images from a given album" in {
    albumService.removePhotosFromAlbum(album1.id, Seq(photo1.id)).flatMap {
      case Right(_) ⇒
        photoService.photosByAlbum(album1.id, 1, 3).map {
          case Right(photos) ⇒
            photos.items should not contain photo1
          case Left(ex) ⇒ fail(s"Failed to  ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"Images were not removed ${ex.msg}")
    }
  }

  it should "still contain remaining photo" in {
    photoService.photosByAlbum(album1.id, 1, 3).map {
      case Right(photos) ⇒ photos.items should contain(photo2)
      case Left(ex) ⇒ fail(s"Failed to fetch images ${ex.msg}")
    }
  }
}
