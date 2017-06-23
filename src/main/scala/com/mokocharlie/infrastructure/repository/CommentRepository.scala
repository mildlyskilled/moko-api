package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.connection.Database
import com.mokocharlie.domain.{Comment, Page}
import com.mokocharlie.model.Page

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._

import scala.util.{Failure, Success}


trait CommentRepository extends Database
  with PhotoRepository
  with AlbumRepository
  with AlbumPhotoRepository {


  class CommentTable(tag: Tag) extends Table[Comment](tag, "common_comment") {

    def id = column[Long]("comment_id", O.PrimaryKey, O.AutoInc)

    def imageID = column[Long]("image_id")

    def comment = column[String]("image_comment")

    def author = column[String]("comment_author")

    def createdAt = column[Timestamp]("comment_date")

    def approved = column[Boolean]("comment_approved")

    def * = (id, imageID, comment, author, createdAt, approved) <>((Comment.apply _).tupled, Comment.unapply)

    def imageFK = foreignKey("image_fk", imageID, photos)(_.id)

  }

  lazy val comments = TableQuery[CommentTable]

  object CommentDAO {


    def getMostRecent(page:Int = 0, limit:Int = 6): Future[Page[Comment]] = {

      val offset = limit * page

      val commentQuery = comments.filter(_.approved).sortBy(_.createdAt.desc.nullsFirst)

      for {
        total <- db.run(commentQuery.groupBy(_ => 0).map(_._2.length).result)
        comments <- db.run(commentQuery.drop(offset).take(limit).result)
      } yield Page(comments, page, offset, total.headOption)
    }

    def findCommentsByImageID(imageID: Long, page: Int, limit: Int): Future[Page[Comment]] = {

      val offset = limit * page

      val commentQuery = for {
        photoComments <- comments
        photo <- photos filter (_.id === imageID) if photo.id === photoComments.imageID
      } yield photoComments

      val totalComments = db.run(commentQuery.groupBy(_ => 0).map(_._2.length).result).map(_.head).value match {
        case Some(Success(count)) => count
        case Some(Failure(_)) => 0
        case None => 0
      }

      for {
      comments <- db.run(commentQuery.result)
      } yield Page(comments, page, offset, Some(totalComments))

    }

    def findCommentsByAlbumID(albumID: Long): Future[Seq[Comment]] = {

      // first get images in given album
      val photoJoin = for {
        p <- photos
        pa <- photoAlbums if p.id === pa.photoID
        a <- albums filter (_.id === albumID) if pa.albumID === a.id
      } yield p.id

      val commentQuery = for {
        comment <- comments filter (_.approved) if comment.imageID in photoJoin
      } yield comment

      db.run(commentQuery.result)
    }

    def findCommentByID(id: Long): Future[Option[Comment]] = {
      db.run(comments.filter(_.id === id).result.headOption)
    }

  }

}