package com.mokocharlie.infrastructure.service

import java.sql.Timestamp

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, PhotoRepository}

import scala.collection.immutable.Seq

class AlbumService(albumRepo: DBAlbumRepository, photoService: PhotoService)(
    implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def create(album: Album): ServiceResponse[Long] = {
    val cover = album.cover.map { photo ⇒
      photoService.create(photo)
    }.flatten
    dbExecute {
      albumRepo.add(
        album.label,
        album.description,
        album.createdAt,
        cover,
        album.published,
        album.featured)
    }
  }

  def list(page: Int, limit: Int, exclude: Seq[Long] = Seq.empty): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.list(page, limit, exclude))

  def albumById(id: Long): ServiceResponse[Option[Album]] =
    dbExecute(albumRepo.albumById(id))

  def featuredAlbums(pageNumber: Int, limit: Int): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.featuredAlbums(pageNumber, limit))

  def collectionAlbums(collectionId: Long, page: Int, limit: Int): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.collectionAlbums(collectionId, page, limit))
}
