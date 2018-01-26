package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.DBCollectionRepository
import com.typesafe.config.ConfigFactory
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor

@DoNotDiscover
class CollectionServiceTest extends AsyncFlatSpec with TestDBUtils with TestFixtures with Matchers {
  implicit val system: ActorSystem = ActorSystem("PhotoTestSystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val config = ConfigFactory.load()
  val collectionRepo = new DBCollectionRepository(config)
  val collectionService = new CollectionService(collectionRepo)

  behavior of "Colletion Service"

  "Collection Service" should "return a list of collections" in {
    collectionService.createOrUpdate(collection1).flatMap {
      case Right(_) ⇒
        collectionService.list(1, 3).map {
          case Right(collection) ⇒ collection.items should contain(collection1)
          case Left(ex) ⇒ fail(s"Could not retrieve collections ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"Could not create new collection ${ex.msg}")
    }
  }

  it should "update collection given a collection" in {
    collectionService.createOrUpdate(collection1.copy(name = "Update from test")).flatMap {
      case Right(_) ⇒
        collectionService.collectionById(collection1.id).map {
          case Right(coll) ⇒ coll.name shouldBe "Update from test"
          case Left(ex) ⇒ fail(s"Collection should be retrieved ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"Collection should be updated ${ex.msg}")
    }
  }
}
