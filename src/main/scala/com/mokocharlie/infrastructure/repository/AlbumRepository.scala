package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

import scala.collection.immutable.Seq

trait AlbumRepository {
  def list(
      page: Int,
      limit: Int,
      exclude: Seq[Long] = Seq.empty,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]]

  def collectionAlbums(
      collectionID: Long,
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]]

  def albumById(albumID: Long): RepositoryResponse[Album]

  def albumById(albumID: Option[Long]): RepositoryResponse[Album]

  def featuredAlbums(
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]]

  def create(album: Album): RepositoryResponse[Long]

  def update(album: Album): RepositoryResponse[Long]

  def savePhotosToAlbum(albumId: Long, photoIds: Seq[Long]): RepositoryResponse[Seq[Int]]

  def removePhotosFromAlbum(albumId: Long, photoIds: Seq[Long]): RepositoryResponse[Seq[Int]]
}


