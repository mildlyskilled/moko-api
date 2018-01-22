package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.{CommentRepository, DBAlbumRepository, DBPhotoRepository}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class AlbumServiceTest
    extends AsyncFlatSpec
    with Matchers
    with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val ec: ExecutionContext = system.dispatcher
  val config: Config = ConfigFactory.load()
  val photoRepository = new DBPhotoRepository(config)
  val albumRepository = new DBAlbumRepository(config, photoRepository)
  val commentRepository = new CommentRepository(config)
  val photoService = new PhotoService(photoRepository, commentRepository)
  val albumService = new AlbumService(albumRepository, photoService)

  behavior of "AlbumService"

  logger.info(
    s"""Running test on
       |${config.getString("mokocharlie.db.host")} with
       |user: ${config.getString("mokocharlie.db.user")} and
       |password: ${config.getString("mokocharlie.db.password")}""".stripMargin)


  "AlbumService" should "create new entries in the database" in {
    albumService.create(
      TestFixtures.album1
    ).map{
      case Right(id) ⇒ id shouldBe 1
      case Left(_) ⇒ fail("An album should be created")
    }
  }

  it should "eventually return a list of albums" in {
    albumService.list(1, 10).map {
      case Right(x) ⇒ x.items should have size 1
      case Left(_) ⇒ fail("Album service must return a  successful result")
    }
  }

  it should "eventually return a EmptyResultError when given a non-existent id " in {
    albumService.albumById(99999999).map { x ⇒
      x shouldBe Left(EmptyResultSet("Could not find album with given id: 99999999"))
    }
  }
}
