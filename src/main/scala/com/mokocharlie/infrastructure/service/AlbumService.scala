package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.{Album, Photo}
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.DBAlbumRepository

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContextExecutor

class AlbumService(albumRepo: DBAlbumRepository, photoService: PhotoService)(
    implicit override val system: ActorSystem)
    extends MokoCharlieService {

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  def createOrUpdate(album: Album): ServiceResponse[Long] = {
    album.cover
      .map { c ⇒
        photoService.createOrUpdate(c).map {
          case Right(id) ⇒
            albumRepo.create(
              album.label,
              album.description,
              album.createdAt,
              Some(id),
              album.published,
              album.featured)
          case Left(e) ⇒ Left(e)
        }
      }
      .getOrElse {
        dbExecute {
          albumRepo
            .albumById(album.id)
            .map(albumRepo.update)
            .getOrElse {
              albumRepo.create(
                album.label,
                album.description,
                album.createdAt,
                None,
                album.published,
                album.featured)

            }
        }
      }
  }

  def list(page: Int, limit: Int, exclude: Seq[Long] = Seq.empty): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.list(page, limit, exclude))

  def albumById(id: Long): ServiceResponse[Album] =
    dbExecute(albumRepo.albumById(id))

  def featuredAlbums(pageNumber: Int, limit: Int): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.featuredAlbums(pageNumber, limit))

  def collectionAlbums(collectionId: Long, page: Int, limit: Int): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.collectionAlbums(collectionId, page, limit))

  def savePhotosToAlbum(albumId: Long, photos: Seq[Long]): ServiceResponse[Unit] =
    dbExecute(albumRepo.savePhotosToAlbum(albumId, photos))

}
