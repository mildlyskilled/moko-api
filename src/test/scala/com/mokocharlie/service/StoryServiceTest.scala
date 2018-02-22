package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.db.DBStoryRepository
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContextExecutor

@DoNotDiscover
class StoryServiceTest extends AsyncFlatSpec with Matchers with TestDBUtils with TestFixtures {

  implicit val system: ActorSystem = ActorSystem("StorySystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val storyRepo = new DBStoryRepository(config)
  val storyService = new StoryService(storyRepo)

  behavior of "Story Service"

  "Story Service " should "retrieve all stories" in {
    storyService.list(1, 10, Some(true)).map{
      case Right(stories) ⇒ stories should have size 3
      case Left(ex) ⇒ fail(s"It should retrieve stories ${ex.msg}")
    }
  }
}
