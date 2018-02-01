package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.{Comment, Photo}
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.{CommentRepository, DBPhotoRepository}
import com.typesafe.scalalogging.StrictLogging

class PhotoService(photoRepo: DBPhotoRepository, commentRepo: CommentRepository)(
    implicit override val system: ActorSystem)
    extends MokoCharlieService
    with StrictLogging {

  def createOrUpdate(photo: Photo): ServiceResponse[Long] =
    dbExecute {
      photoRepo.photoById(photo.id) match {
        case Right(_) ⇒
          logger.info(s"Photo exists updating ID ${photo.id}")
          photoRepo.update(photo)
        case Left(EmptyResultSet(_)) ⇒
          logger.info(s"Photo not found, creating ${photo.name}")
          photoRepo.create(
            photo.imageId,
            photo.name,
            photo.path,
            photo.caption,
            photo.createdAt,
            photo.updatedAt,
            photo.deletedAt,
            photo.published,
            photo.cloudImage,
            photo.ownerId)
        case Left(e) ⇒
          logger.warn(s"Photo not created ${e.msg}")
          Left(e)
      }
    }

  def list(page: Int, limit: Int): ServiceResponse[Page[Photo]] =
    dbExecute(photoRepo.list(page, limit))

  def photoById(id: Long): ServiceResponse[Photo] =
    dbExecute(photoRepo.photoById(id))

  def photoById(id: String): ServiceResponse[Photo] =
    dbExecute(photoRepo.photoById(id))

  def photosByAlbum(id: Long, pageNumber: Int, limit: Int): ServiceResponse[Page[Photo]] =
    dbExecute(photoRepo.photosByAlbumId(id, pageNumber, limit))

}
