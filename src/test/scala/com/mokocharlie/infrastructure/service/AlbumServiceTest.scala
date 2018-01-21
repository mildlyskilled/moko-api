package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, DBPhotoRepository}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class AlbumServiceTest
    extends AsyncFlatSpec
    with BeforeAndAfterAll
    with Matchers
    with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val ec: ExecutionContext = system.dispatcher
  val config: Config = ConfigFactory.load()

  override def beforeAll(): Unit = {}

  behavior of "AlbumService"

  logger.info(
    s"""Running test on
       |${config.getString("mokocharlie.db.host")} with
       |${config.getString("mokocharlie.db.user")} and
       |${config.getString("mokocharlie.db.password")}""".stripMargin)
  val photoRepository = new DBPhotoRepository(config)
  val albumRepository = new DBAlbumRepository(config, photoRepository)
  val albumService = new AlbumService(albumRepository)

  "AlbumService" should "eventually return a list of albums" in {
    albumService.list(1, 10).map {
      case Right(x) ⇒ x.items should have size 10
      case Left(_) ⇒ fail("Album service must return a  successful result")
    }
  }

  it should "eventually return a EmptyResultError when given a non-existent id " in {
    albumService.albumById(99999999).map { x ⇒
      x shouldBe Left(EmptyResultSet("Could not find album with given id: 1000"))
    }
  }
}
