package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.{Comment, Photo}
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.domain.common.ServiceResponse.{RepositoryResponse, ServiceResponse}
import com.mokocharlie.infrastructure.repository.{CommentRepository, DBPhotoRepository}

class PhotoService(photoRepo: DBPhotoRepository, commentRepo: CommentRepository)(
    implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def createOrUpdate(photo: Photo): ServiceResponse[Long] =
    dbExecute {
      photoRepo.photoById(photo.id) match {
        case Right(maybePhoto) ⇒
          maybePhoto.map(photoRepo.update).getOrElse {
            photoRepo.create(
              photo.name,
              photo.path,
              photo.caption,
              photo.createdAt,
              photo.updatedAt,
              photo.deletedAt,
              photo.published,
              photo.cloudImage,
              photo.ownerId)
          }
        case Left(EmptyResultSet(_)) ⇒
          photoRepo.create(
            photo.name,
            photo.path,
            photo.caption,
            photo.createdAt,
            photo.updatedAt,
            photo.deletedAt,
            photo.published,
            photo.cloudImage,
            photo.ownerId)
        case Left(e) ⇒ Left(e)
      }
    }

  def list(page: Int, limit: Int): ServiceResponse[Page[Photo]] =
    dbExecute(photoRepo.list(page, limit))

  def photoById(id: Long): ServiceResponse[Option[Photo]] =
    dbExecute(photoRepo.photoById(id))

  def photoById(id: String): ServiceResponse[Option[Photo]] =
    dbExecute(photoRepo.photoById(id))

  def photosByAlbum(id: Long, pageNumber: Int, limit: Int): ServiceResponse[Page[Photo]] =
    dbExecute(photoRepo.photosByAlbumId(id, pageNumber, limit))

  def commentsByPhotoId(id: Long, pageNumber: Int, limit: Int): ServiceResponse[Page[Comment]] =
    dbExecute(commentRepo.findCommentsByImageID(id, pageNumber, limit))
}
