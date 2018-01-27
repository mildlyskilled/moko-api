package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Comment
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.CommentRepository

class CommentService(repo: CommentRepository)(implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def mostRecentComments(page: Int, limit: Int, approvedOnly: Option[Boolean]): ServiceResponse[Page[Comment]] =
    dbExecute(repo.getMostRecent(page, limit, approvedOnly))

  def commentById(id: Long): ServiceResponse[Option[Comment]] =
    dbExecute(repo.findCommentByID(id))
}
