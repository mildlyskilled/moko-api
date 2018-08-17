package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.db.DBFavouriteRepository
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor

@DoNotDiscover
class FavouriteServiceTest extends AsyncFlatSpec with TestDBUtils with TestFixtures with Matchers {
  implicit val system: ActorSystem = ActorSystem("FavouriteTestSystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val favouriteRepo = new DBFavouriteRepository(config)
  val favouriteService = new FavouriteService(favouriteRepo, clock)
  behavior of "FavouriteService"

  "FavouriteService" should "create a favourite" in {
    favouriteService.addFavourite(favourite1.user.id, favourite1.photo.id).map {
      case Right(id) ⇒ id shouldBe 1
      case Left(ex) ⇒ fail(s"A favourite should be added ${ex.msg}")
    }
  }

  it should "not add a favourite if it already exists" in {
    favouriteService.addFavourite(favourite1.user.id, favourite1.photo.id).map {
      case Left(ex) ⇒ ex.msg shouldBe "This photo already exists in user favourites"
      case Right(_) ⇒ fail(s"a favourite should be NOT added")
    }
  }

  it should "return favourites by photo" in {
    favouriteService.addFavourite(userId = 1, photoId = 2).flatMap{
      case Right(_) ⇒
        favouriteService.imageFavourites(favourite1.photo.id, 1, 10).map {
          case Right(favouritePage) ⇒ favouritePage.items should have size 1
          case Left(ex) ⇒ fail(s"Favourites should have been returned ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"A new favourite should be created ${ex.msg}")
    }

  }
}
