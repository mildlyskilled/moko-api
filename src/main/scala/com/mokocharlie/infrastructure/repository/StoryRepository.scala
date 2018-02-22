package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Story
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait StoryRepository {
  def list(page: Int, limit: Int, publishedOnly: Option[Boolean]): RepositoryResponse[Page[Story]]

  def storyById(id: Long): RepositoryResponse[Story]

  def create(story: Story): RepositoryResponse[Long]

  def update(story: Story): RepositoryResponse[Long]

}
