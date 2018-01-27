package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.{DBCommentRepository, DBPhotoRepository}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor

@DoNotDiscover
class CommentServiceTest extends AsyncFlatSpec with Matchers with TestFixtures {

  implicit val system: ActorSystem = ActorSystem("PhotoTestSystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val config: Config = ConfigFactory.load()
  val photoRepo = new DBPhotoRepository(config)
  val commentRepo = new DBCommentRepository(config)
  val commentService = new CommentService(commentRepo)

  behavior of "CommentService"

  "Comment Service " should "retrieve most recent comments" in {
    commentService.mostRecentComments(1, 2, Some(true)).map {
      case Right(comments) ⇒ comments.items should not be empty
      case Left(ex) ⇒ fail(s"Could not retrieve most recent comments ${ex.msg}")
    }
  }

}
