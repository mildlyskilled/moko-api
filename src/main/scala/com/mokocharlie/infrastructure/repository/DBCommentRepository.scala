package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.{Comment, Photo}
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.DatabaseServiceError
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc._

trait CommentRepository {
  def getMostRecent(
      page: Int = 0,
      limit: Int = 6,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def findCommentsByImageID(
      imageID: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def findCommentsByAlbumID(
      albumID: Long,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def findCommentByID(id: Long): RepositoryResponse[Option[Comment]]
}

class DBCommentRepository(override val config: Config)
    extends CommentRepository
    with JdbcRepository
    with RepoUtils
    with StrictLogging {

  def getMostRecent(
      page: Int = 1,
      limit: Int = 6,
      approvedOnly: Option[Boolean] = None): RepositoryResponse[Page[Comment]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val res = sql"""
           $defaultSelect
            ${selectApproved(approvedOnly)}
           $defaultOrder
           LIMIT ${dbPage(page)}, ${rowCount(page, limit)}
        """.map(toComment).list.apply()
        Right(Page(res, page, dbPage(page), Some(limit)))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }

    }

  def findCommentsByImageID(
      imageID: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]] = ???

  def findCommentsByAlbumID(albumID: Long, approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]] = ???

  def findCommentByID(id: Long): RepositoryResponse[Option[Comment]] = ???

  private val defaultSelect: SQLSyntax =
    sqls"""
          | SELECT
          | c.comment_id,
          | c.image_comment,
          | c.comment_author,
          | c.comment_date,
          | c.comment_approved,
          | c.image_id,
          | p.id AS photo_id,
          |	p.image_id AS legacy_image_id,
          |	p.name AS photo_name,
          |	p.caption AS photo_caption,
          |	p.created_at AS photo_created_at,
          |	p.deleted_at AS photo_deleted_at,
          |	p.`owner` AS photo_owner,
          |	p.path AS photo_path,
          |	p.`updated_at` AS photo_updated_at,
          |	p.cloud_image,
          |	p.published AS photo_published
          | FROM common_comment AS c
          | LEFT JOIN common_photo AS p ON p.id = c.image_id
        """.stripMargin

  private val defaultOrder: SQLSyntax = sqls"ORDER BY c.comment_date DESC"

  private def toComment(rs: WrappedResultSet): Comment =
    Comment(
      commentID = rs.long("comment_id"),
      comment = rs.string("image_comment"),
      author = rs.string("comment_author"),
      createdAt = rs.timestamp("comment_date"),
      approved = rs.boolean("comment_approved"),
      photo = Photo(
        rs.int("photo_id"),
        rs.stringOpt("legacy_image_id"),
        rs.string("photo_name"),
        rs.stringOpt("photo_path"),
        rs.string("photo_caption"),
        rs.timestamp("photo_created_at"),
        rs.timestampOpt("photo_updated_at"),
        rs.int("photo_owner"),
        rs.boolean("photo_published"),
        rs.timestampOpt("photo_deleted_at"),
        rs.stringOpt("cloud_image")
      )
    )

  private def selectApproved(approvedOnly: Option[Boolean], joiner: String = "WHERE") =
    approvedOnly
      .map { p ⇒
        val j = joiner match {
          case "AND" ⇒ sqls"AND"
          case _ ⇒ sqls"WHERE"
        }
        sqls" $j c.comment_approved = $p"
      }
      .getOrElse(sqls"")

}
