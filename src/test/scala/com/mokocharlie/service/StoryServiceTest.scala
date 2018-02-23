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

  "Story Service " should "Create new story" in {
    storyService.createOrUpdate(story1).map {
      case Right(id) ⇒ id shouldBe 1
      case Left(error) ⇒ fail(s"A photo story should have been created ${error.msg}")
    }
  }

  it should "Retrieve all stories" in {
    storyService.list(1, 10, Some(true)).map {
      case Right(stories) ⇒ stories should have size 1
      case Left(ex) ⇒ fail(s"It should retrieve stories ${ex.msg}")
    }
  }

  it should "Retrieve story by id" in {
    storyService.storyById(1).map {
      case Right(story) ⇒
        story.id shouldBe 1
        story.name shouldBe "Some photo story"
      case Left(error) ⇒ fail(s"A story should have been retrieved ${error.msg}")
    }
  }

  it should "Update a story" in {
    storyService.storyById(1).flatMap {
      case Right(story) ⇒
        storyService.createOrUpdate(story.copy(name = "Updated story name")).flatMap {
          case Right(id) ⇒
            storyService.storyById(id).map {
              case Right(updatedStory) ⇒ updatedStory.name shouldBe "Updated story name"
              case Left(error) ⇒ fail(s"A story should have been retrieved ${error.msg}")
            }
          case Left(error) ⇒ fail(s"Story should have been updated ${error.msg}")
        }
      case Left(error) ⇒ fail(s"Story should have been retrieved ${error.msg}")
    }
  }

}
