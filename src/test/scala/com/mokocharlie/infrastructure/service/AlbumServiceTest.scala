package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.infrastructure.repository.{
  CommentRepository,
  DBAlbumRepository,
  DBPhotoRepository
}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class AlbumServiceTest
    extends AsyncFlatSpec
    with Matchers
    with StrictLogging
    with DBTestUtils
    with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val ec: ExecutionContext = system.dispatcher
  val config: Config = ConfigFactory.load()
  val photoRepository = new DBPhotoRepository(config)
  val albumRepository = new DBAlbumRepository(config, photoRepository)
  val commentRepository = new CommentRepository(config)
  val photoService = new PhotoService(photoRepository, commentRepository)
  val albumService = new AlbumService(albumRepository, photoRepository)

  behavior of "AlbumService"

  logger.info(s"""Running test on
       |${config.getString("mokocharlie.db.host")} with
       |user: ${config.getString("mokocharlie.db.user")} and
       |password: ${config.getString("mokocharlie.db.password")}""".stripMargin)

  "AlbumService" should "create new album with a cover" in {
    albumService
      .createOrUpdate(
        album1
      )
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
        case Left(_) ⇒ fail("An album should have been created")
      }
  }

  it should "eventually return a list of albums" in {
    albumService.list(1, 10).map {
      case Right(x) ⇒ x.items should have size 2
      case Left(_) ⇒ fail("Album service must return a  successful result")
    }
  }

  it should "eventually return a EmptyResultError when given a non-existent id " in {
    albumService.albumById(99999999).map { x ⇒
      x shouldBe Left(EmptyResultSet("Could not find album with given id: 99999999"))
    }
  }

  it should "eventually be updated when values are changed" in {
    albumService.createOrUpdate(album1.copy(label = "Test update")).flatMap {
      case Right(id) ⇒
        albumService.albumById(id).map {
          case Right(Some(album)) ⇒ album.label shouldBe "Test Update"
          case Left(_) ⇒ fail("An album should have been found")
        }
      case Left(e) ⇒ fail(s"The update failed $e")
    }

  }
}
