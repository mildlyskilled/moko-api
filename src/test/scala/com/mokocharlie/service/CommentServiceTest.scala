package com.mokocharlie.service

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

  "Comment Service " should "create a new comment in" in {
    commentService.createOrUpdate(comment1).map {
      case Right(id) ⇒ id shouldBe comment1.id
      case Left(ex) ⇒ fail(s"A comment should have been created ${ex.msg}")
    }
  }

  it should "retrieve most recent comments" in {
    commentService.mostRecentComments(1, 2, Some(true)).map {
      case Right(comments) ⇒ comments should not be empty
      case Left(ex) ⇒ fail(s"Could not retrieve most recent comments ${ex.msg}")
    }
  }

  it should "retrieve comments given an photo id" in {
    commentService.commentsByImage(photo1.id, 1, 20, Some(true)).map {
      case Right(commentsPage) ⇒ commentsPage.isEmpty shouldBe false
      case Left(ex) ⇒ fail(s"Should return comments ${ex.msg}")
    }
  }

  it should "create a comment" in {
    commentService.createOrUpdate(comment2).flatMap {
      case Right(id) ⇒
        commentService.commentById(id).map {
          case Right(comment) ⇒ comment.comment shouldBe "This is a second comment"
          case Left(ex) ⇒ fail(s"Should have returned a comment ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"A comment should have been created ${ex.msg}")
    }
  }

  it should "get comments given an album id" in {
    commentService.commentsByAlbum(album1.id, 1, 10, Some(true)).map {
      case Right(comments) ⇒ comments.isEmpty shouldBe false
      case Left(ex) ⇒ fail(s"Comments should have been retrieved ${ex.msg}")
    }
  }
}
