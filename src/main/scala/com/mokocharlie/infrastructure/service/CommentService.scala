package com.mokocharlie.infrastructure.service

import java.sql.Timestamp

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Comment
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.CommentRepository
import com.typesafe.scalalogging.StrictLogging

class CommentService(repo: CommentRepository)(implicit override val system: ActorSystem)
    extends MokoCharlieService
    with StrictLogging {

  def createOrUpdate(comment: Comment): ServiceResponse[Long] =
    dbExecute {
      repo.commentById(comment.id) match {
        case Right(_) ⇒
          logger.info(s"Comment exists updating ID ${comment.id}")
          repo.update(comment)
        case Left(_) ⇒
          logger.info(s"Comment not found creating new one")
          repo.create(
            comment.photo.id,
            comment.comment,
            comment.author,
            comment.createdAt,
            comment.approved
          )
      }
    }

  def mostRecentComments(
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): ServiceResponse[Page[Comment]] =
    dbExecute(repo.getMostRecent(page, limit, approvedOnly))

  def commentById(id: Long): ServiceResponse[Comment] =
    dbExecute(repo.commentById(id))

  def commentsByImage(
      photoId: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): ServiceResponse[Page[Comment]] =
    dbExecute(repo.commentsByImage(photoId, page, limit, approvedOnly))

  def commentsByAlbum(
      albumId: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): ServiceResponse[Page[Comment]] =
    dbExecute(repo.commentsByAlbum(albumId, page, limit, approvedOnly))
}
