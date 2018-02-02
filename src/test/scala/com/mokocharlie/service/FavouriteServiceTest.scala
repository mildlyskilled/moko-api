package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.DBFavouriteRepository
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor

@DoNotDiscover
class FavouriteServiceTest extends AsyncFlatSpec with TestDBUtils with TestFixtures with Matchers {
  implicit val system: ActorSystem = ActorSystem("FavouriteTestSystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val config: Config = ConfigFactory.load()
  val favouriteRepo = new DBFavouriteRepository(config)
  val favouriteService = new FavouriteService(favouriteRepo)
  behavior of "FavouriteService"

  "FavouriteService" should "create a favourite" in {
    favouriteService.addFavourite(favourite1).map{
      case Right(id) ⇒ id shouldBe 1
      case Left(ex) ⇒ fail(s"A favourite should be added ${ex.msg}")
    }
  }
}
