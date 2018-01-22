package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.{MokoModel, Page}
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, PhotoRepository}

import scala.concurrent.Future
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContextExecutor

class AlbumService(albumRepo: DBAlbumRepository, photoService: PhotoService)(
    implicit override val system: ActorSystem)
    extends MokoCharlieService {

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  def createOrUpdate(album: Album): ServiceResponse[Long] = {
    val cover = album.cover.map(photoService.createOrUpdate)

    dbExecute {
      albumRepo
        .albumById(album.id) match {
        case Right(_) ⇒ albumRepo.update(album)
        case Left(EmptyResultSet(_)) ⇒
          albumRepo.create(
            album.label,
            album.description,
            album.createdAt,
            cover,
            album.published,
            album.featured)
        case Left(ex) ⇒ Left(ex)
      }
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
