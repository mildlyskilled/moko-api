package com.mokocharlie.service

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
      .map { cover ⇒
        photoService.createOrUpdate(cover).map {
          case Right(_) ⇒ albumRepo.create(album)
          case Left(e) ⇒ Left(e)
        }
      }
      .getOrElse {
        dbExecute {
          albumRepo
            .albumById(album.id)
            .map(_ ⇒ albumRepo.update(album))
            .getOrElse(albumRepo.create(album))
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

  def savePhotosToAlbum(albumId: Long, photoIds: Seq[Long]): ServiceResponse[Seq[Int]] =
    dbExecute(albumRepo.savePhotosToAlbum(albumId, photoIds))

  def removePhotosFromAlbum(albumId: Long, photoIds: Seq[Long]): ServiceResponse[Seq[Int]] =
    dbExecute(albumRepo.removePhotosFromAlbum(albumId, photoIds))

}
