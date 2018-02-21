package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.Photo
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

import scala.collection.immutable.Seq

trait PhotoRepository {
  def list(
      page: Int,
      limit: Int,
      exclude: Seq[Long] = Seq.empty,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]]

  def photoById(imageID: String): RepositoryResponse[Photo]

  def photoById(id: Long): RepositoryResponse[Photo]

  def photosByUserId(
      userId: Long,
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean] = None): RepositoryResponse[Page[Photo]]

  def photosByAlbumId(
      albumID: Long,
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]]

  def create(
      imageId: Option[String],
      name: String,
      path: Option[String],
      caption: String,
      createdAt: Timestamp,
      updatedAt: Option[Timestamp],
      deletedAt: Option[Timestamp],
      published: Boolean,
      cloudImage: Option[String],
      owner: Long): RepositoryResponse[Long]

  def update(photo: Photo): RepositoryResponse[Long]

}
