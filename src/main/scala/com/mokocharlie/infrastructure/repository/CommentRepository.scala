package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Comment
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future


class CommentRepository(override val config: Config)
  extends JdbcRepository
  with StrictLogging {

    def getMostRecent(page:Int = 0, limit:Int = 6): RepositoryResponse[Page[Comment]] = ???

    def findCommentsByImageID(imageID: Long, page: Int, limit: Int): RepositoryResponse[Page[Comment]] = ???

    def findCommentsByAlbumID(albumID: Long): RepositoryResponse[Page[Comment]] = ???

    def findCommentByID(id: Long): RepositoryResponse[Option[Comment]] = ???

}