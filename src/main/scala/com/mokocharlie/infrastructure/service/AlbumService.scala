package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.DBAlbumRepository

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContextExecutor, Future}

class AlbumService(albumRepo: DBAlbumRepository, photoService: PhotoService)(
    implicit override val system: ActorSystem)
    extends MokoCharlieService {

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  def create(album: Album): ServiceResponse[Long] = {
    val cover: Future[Option[Long]] = album.cover
      .map { photo ⇒
        photoService.create(photo).map {
          case Right(id) ⇒ Some(id)
          case Left(_) ⇒ None
        }
      }
      .getOrElse(Future(None))

    dbExecute {
      cover.map { id ⇒
        albumRepo.create(
          album.label,
          album.description,
          album.createdAt,
          id,
          album.published,
          album.featured)
      }
    }.flatten
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
