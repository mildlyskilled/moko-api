package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.CollectionRepository
import com.typesafe.config.ConfigFactory
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor

@DoNotDiscover
class CollectionServiceTest extends AsyncFlatSpec with TestDBUtils with TestFixtures with Matchers {
  implicit val system: ActorSystem = ActorSystem("PhotoTestSystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val config = ConfigFactory.load()
  val collectionRepo = new CollectionRepository(config)
  val collectionService = new CollectionService(collectionRepo)

  behavior of "Colletion Service"

  "Collection Service" should "return a list of collections" in {
    collectionService.list(1, 3).flatMap {
      case Right(collection) ⇒ collection.items should contain(collection1)
      case Left(ex) ⇒ fail(s"Could not retrieve collections ${ex.msg}")
    }
  }
}
