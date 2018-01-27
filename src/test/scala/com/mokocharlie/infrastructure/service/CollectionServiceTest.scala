package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository._
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor
import scala.collection.immutable.Seq

@DoNotDiscover
class CollectionServiceTest extends AsyncFlatSpec with TestDBUtils with TestFixtures with Matchers {
  implicit val system: ActorSystem = ActorSystem("PhotoTestSystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val config: Config = ConfigFactory.load()
  val collectionRepo = new DBCollectionRepository(config)
  val photoRepo = new DBPhotoRepository(config)
  val commentRepo = new DBCommentRepository(config)
  val photoService = new PhotoService(photoRepo, commentRepo)
  val albumRepo = new DBAlbumRepository(config, photoRepo)
  val collectionService = new CollectionService(collectionRepo)
  val albumService = new AlbumService(albumRepo, photoService)

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

  it should "add albums to collection" in {
    albumService.createOrUpdate(album1).flatMap {
      case Right(_) ⇒
        albumService.createOrUpdate(album2).flatMap {
          case Right(_) ⇒
            albumService.list(1, 5).flatMap {
              case Right(albums) ⇒
                collectionService
                  .saveAlbumToCollection(collection1.id, albums.items.map(_.id))
                  .flatMap {
                    case Right(_) ⇒
                      albumService.collectionAlbums(collection1.id, 1, 5).map {
                        case Right(albumPage) ⇒
                          albumPage.items should contain allOf (album1, album2)
                        case Left(ex) ⇒ fail(s"Could not retrieve collection albums ${ex.msg}")
                      }
                    case Left(ex) ⇒ fail(s"Should retrieve albums in collection ${ex.msg}")
                  }
              case Left(ex) ⇒ fail(s"Should retrieve albums ${ex.msg}")
            }
          case Left(ex) ⇒ fail(s"Should have creaed album2 ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"Should have created album1 ${ex.msg}")
    }
  }

  it should "remove albums form collection" in {
    collectionService.removeAlbumFromCollection(collection1.id, Seq(album1.id, album2.id)).flatMap {
      case Right(_) ⇒ albumService.collectionAlbums(collection1.id, 1, 5).map{
        case Right(albumPage) ⇒ albumPage.items should not contain allOf (album1, album2)
        case Left(ex) ⇒ fail(s"Could not retrieve albums in collection ${ex.msg}")
      }
      case Left(ex) ⇒ fail(s"Could not remove album from collection ${ex.msg}")
    }
  }
}
