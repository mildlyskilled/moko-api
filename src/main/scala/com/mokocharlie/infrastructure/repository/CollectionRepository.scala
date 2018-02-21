package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

import scala.collection.immutable.Seq

trait CollectionRepository {
  def list(
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): RepositoryResponse[Page[Collection]]

  def collectionById(id: Long): RepositoryResponse[Collection]

  def featuredCollections(page: Int = 1, limit: Int = 10): RepositoryResponse[Page[Collection]]

  def create(collection: Collection): RepositoryResponse[Long]

  def update(collection: Collection): RepositoryResponse[Long]

  def saveAlbumToCollection(collectionId: Long, albums: Seq[Long]): RepositoryResponse[Seq[Int]]

  def removeAlbumFromCollection(collectionId: Long, albums: Seq[Long]): RepositoryResponse[Seq[Int]]

}
