package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.{RepositoryResponse, ServiceResponse}
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, PhotoRepository}

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContextExecutor

class AlbumService(albumRepo: DBAlbumRepository, photoRepository: PhotoRepository)(
    implicit override val system: ActorSystem)
    extends MokoCharlieService {

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  def createOrUpdate(album: Album): ServiceResponse[Long] = {
    dbExecute {
      val cover = album.cover.map(p ⇒ photoRepository.photoById(p.id)).flatMap {
        case Right(response) ⇒ response.map(_.id)
        case Left(_) ⇒ None
      }

      val albumUpdate: Option[RepositoryResponse[Long]] = albumRepo
        .albumById(album.id) match {
        case Right(albumOption) ⇒ albumOption.map(albumRepo.update)
        case Left(_) ⇒ None
      }

      albumUpdate.getOrElse {
        albumRepo.create(
          album.label,
          album.description,
          album.createdAt,
          cover,
          album.published,
          album.featured)
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
