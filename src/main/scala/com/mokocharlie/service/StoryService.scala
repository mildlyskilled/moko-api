package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Story
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.StoryRepository

class StoryService(storyRepository: StoryRepository)(
    implicit val system: ActorSystem)
    extends MokoCharlieService {

  def list(page: Int, limit: Int, publishedOnly: Option[Boolean]): ServiceResponse[Page[Story]] =
    dbExecute(storyRepository.list(page, limit, publishedOnly))

  def createOrUpdate(story: Story): ServiceResponse[Long] =
    dbExecute{
      storyRepository.storyById(story.id).toOption.map {_ ⇒
        storyRepository.update(story)
      }.getOrElse(storyRepository.create(story))
    }
}
