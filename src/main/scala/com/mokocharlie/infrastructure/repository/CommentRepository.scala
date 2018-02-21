package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.Comment
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait CommentRepository {

  def getMostRecent(
      page: Int = 0,
      limit: Int = 6,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def commentsByImage(
      imageID: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def commentsByAlbum(
      albumId: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def commentById(id: Long): RepositoryResponse[Comment]

  def create(
      imageId: Long,
      comment: String,
      author: String,
      postedAt: Timestamp,
      approved: Boolean = false): RepositoryResponse[Long]

  def update(comment: Comment): RepositoryResponse[Long]
}
