package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.{CommentRepository, DBCommentRepository, DBPhotoRepository}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor

@DoNotDiscover
class PhotoServiceTest extends AsyncFlatSpec with TestFixtures with Matchers with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("PhotoTestSystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val config: Config = ConfigFactory.load()
  val photoRepo = new DBPhotoRepository(config)
  val commentRepo = new DBCommentRepository(config)
  val photoService = new PhotoService(photoRepo, commentRepo)

  behavior of "PhotoService"

  "PhotoService" should "save a photo model" in {
    photoService.createOrUpdate(photo1) map {
      case Right(res) ⇒ res shouldBe 1
      case Left(ex) ⇒ fail(s"A photo should have been created ${ex.msg}")
    }
  }

  it should "update an already stored image" in {
    photoService.createOrUpdate(photo1.copy(name = "Photo Service test")) flatMap {
      case Right(res) ⇒
        photoService.photoById(res) map {
          case Right(res2) ⇒ res2.name shouldBe "Photo Service test"
          case Left(ex) ⇒ fail(s"Photo should have been retrieved ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"Image should have been updated ${ex.msg}")
    }
  }
}
