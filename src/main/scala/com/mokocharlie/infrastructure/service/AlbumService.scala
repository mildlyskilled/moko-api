package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Album
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.{AlbumRepository, PhotoRepository}
import scala.collection.immutable.Seq

class AlbumService(albumRepo: AlbumRepository, photoRepo: PhotoRepository)
                  (implicit override val system: ActorSystem) extends MokoCharlieService {

  def list(page: Int, limit: Int, exclude: Seq[Long]): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.list(page, limit, exclude))

  def albumById(id: Long): ServiceResponse[Option[Album]] =
    dbExecute(albumRepo.findAlbumByID(id))

  def featuredAlbums(pageNumber: Int, limit: Int): ServiceResponse[Page[Album]] =
    dbExecute(albumRepo.getFeaturedAlbums(pageNumber, limit))
}
