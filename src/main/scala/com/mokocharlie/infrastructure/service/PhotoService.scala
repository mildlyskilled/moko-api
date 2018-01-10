package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.{Comment, Photo}
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.{CommentRepository, PhotoRepository}

class PhotoService(photoRepo: PhotoRepository, commentRepo: CommentRepository)
                  (implicit override val system: ActorSystem) extends MokoCharlieService {

  def list(page: Int, limit: Int): ServiceResponse[Page[Photo]] =
    dbExecute(photoRepo.list(page, limit))

  def photoById(id: Long): ServiceResponse[Option[Photo]] =
    dbExecute(photoRepo.findPhotoByID(id))

  def photoById(id: String): ServiceResponse[Option[Photo]] =
    dbExecute(photoRepo.findPhotoByImageID(id))

  def photosByAlbum(id: Long, pageNumber:Int, limit: Int): ServiceResponse[Page[Photo]] =
    dbExecute(photoRepo.getPhotosByAlbumId(id, pageNumber, limit))

  def commentsByPhotoId(id: Long, pageNumber: Int, limit: Int): ServiceResponse[Page[Comment]] =
    dbExecute(commentRepo.findCommentsByImageID(id, pageNumber, limit))
}
