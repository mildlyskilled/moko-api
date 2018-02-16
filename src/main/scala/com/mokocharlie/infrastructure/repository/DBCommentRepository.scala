package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.{Comment, Photo}
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
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

  def commentsByImage(
      imageID: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def commentsByAlbum(
      albumId: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]]

  def commentById(id: Long): RepositoryResponse[Comment]

  def create(
      imageId: Long,
      comment: String,
      author: String,
      postedAt: Timestamp,
      approved: Boolean = false): RepositoryResponse[Long]

  def update(comment: Comment): RepositoryResponse[Long]
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
           LIMIT ${dbPage(page)}, ${offset(page, limit)}
        """.map(toComment).list.apply()
        Right(Page(res, page, dbPage(page), Some(limit)))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }

    }

  def commentsByImage(
      photoId: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]] = readOnlyTransaction {
    implicit session ⇒
      try {
        val comments = sql"""
          $defaultSelect
          WHERE p.id = $photoId
          ${selectApproved(approvedOnly, "AND")}
          LIMIT ${dbPage(page)}, ${offset(page, limit)}
          """
          .map(toComment)
          .list
          .apply()
        Right(Page(comments, page, dbPage(page), Some(limit)))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
  }

  def commentsByAlbum(
      albumId: Long,
      page: Int,
      limit: Int,
      approvedOnly: Option[Boolean]): RepositoryResponse[Page[Comment]] = readOnlyTransaction {
    implicit session ⇒
      try {
        val comments =
          sql"""
            $defaultSelect
             LEFT JOIN common_photo_albums AS cpa ON cpa.photo_id = p.id
             WHERE cpa.album_id = $albumId
             ${selectApproved(approvedOnly, "AND")}
             LIMIT ${dbPage(page)}, ${offset(page, limit)}
          """
            .map(toComment)
            .list
            .apply()
        Right(Page(comments, page, dbPage(page), Some(limit)))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
  }

  def commentById(id: Long): RepositoryResponse[Comment] =
    readOnlyTransaction { implicit session ⇒
      try {
        val comment = sql"$defaultSelect WHERE c.comment_id = $id".map(toComment).single.apply()
        comment
          .map(c ⇒ Right(c))
          .getOrElse(Left(EmptyResultSet(s"Could not find comment with id: ${id}")))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }

    }

  def create(
      imageId: Long,
      comment: String,
      author: String,
      postedAt: Timestamp,
      approved: Boolean = false): RepositoryResponse[Long] =
    writeTransaction(3, "Could not create new comment entry") { implicit session ⇒
      try {
        Right {
          sql"""
                INSERT INTO common_comment(image_comment, comment_author, comment_date, comment_approved, image_id)
                VALUES ($comment, $author, $postedAt, $approved, $imageId)
          """.updateAndReturnGeneratedKey
            .apply()
        }
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def update(comment: Comment): RepositoryResponse[Long] =
    writeTransaction(3, s"Could not update comment: ${comment.id}") { implicit session ⇒
      try {
        val res =
          sql"""
            UPDATE common_comment SET
            image_id = ${comment.photo.id},
            image_comment = ${comment.comment},
            comment_author = ${comment.author},
            comment_date = ${comment.createdAt}
            comment_approved = ${comment.approved}
            WHERE comment_id = ${comment.id}
          """.update.apply()
        if (res > 0) Right(comment.id)
        else Left(EmptyResultSet("No rows were updated"))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

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
      id = rs.long("comment_id"),
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
