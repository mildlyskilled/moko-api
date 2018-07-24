package com.mokocharlie.infrastructure.repository.db

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.Photo
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.PhotoRepository
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

import scala.collection.immutable.Seq

class DBPhotoRepository(override val config: Config)
    extends PhotoRepository
    with JdbcRepository
    with RepoUtils
    with StrictLogging {

  def list(
      page: Int,
      limit: Int,
      exclude: Seq[Long] = Seq.empty,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          val photos =
            sql"""
              $defaultSelect
              ${selectPublished(publishedOnly)}
              $defaultOrdering
              LIMIT ${offset(page, limit)}, $limit

        """.map(toPhoto)
              .list()
              .apply()

          Right(Page(photos, page, limit, total().toOption))
        } catch {
          case e: Exception ⇒
            logger.error(s"Error whilst retrieving photos", e)
            Left(DatabaseServiceError(e.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒
        logger.error(s"Error whilst retrieving photos", e)
        Left(DatabaseServiceError(e.getMessage))
    }

  def photoById(imageID: String): RepositoryResponse[Photo] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          sql"""
               $defaultSelect
              WHERE image_id = $imageID
             """
            .map(toPhoto)
            .single
            .apply()
            .map(p ⇒ Right(p))
            .getOrElse(Left(EmptyResultSet(s"Could not find photo with IMAGE_ID $imageID")))
        } catch {
          case e: Exception ⇒
            logger.error("Unable to get photo")
            Left(DatabaseServiceError(e.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒
        logger.error(s"Error whilst retrieving photos", e)
        Left(DatabaseServiceError(e.getMessage))
    }

  def photoById(id: Long): RepositoryResponse[Photo] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          sql"""
               $defaultSelect
              WHERE p.id = $id
             """
            .map(toPhoto)
            .single
            .apply()
            .map(p ⇒ Right(p))
            .getOrElse(Left(EmptyResultSet(s"Could not find photo with id: $id")))
        } catch {
          case e: Exception ⇒
            logger.error("Unable to get photo")
            Left(DatabaseServiceError(e.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒
        logger.error(s"Error whilst retrieving photos", e)
        Left(DatabaseServiceError(e.getMessage))
    }

  def photosByUserId(
      userId: Long,
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean] = None): RepositoryResponse[Page[Photo]] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          val photos = sql"""
               $defaultSelect
               ${selectPublished(publishedOnly)}
              AND owner = $userId
               $defaultOrdering
              LIMIT , $limit
             """.map(toPhoto).list.apply()
          Right(Page(photos, page, limit, total().toOption))
        } catch {
          case e: Exception ⇒
            logger.error("Unable to get photos")
            Left(DatabaseServiceError(e.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒
        logger.error(s"Error whilst retrieving photos", e)
        Left(DatabaseServiceError(e.getMessage))
    }

  def photosByAlbumId(
      albumID: Long,
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          val photos =
            sql"""
            $defaultSelect
            LEFT JOIN common_photo_albums AS cab
            ON p.id = cab.photo_id
            WHERE cab.album_id = $albumID
            ${selectPublished(publishedOnly, "AND")}
            LIMIT ${offset(page, limit)}, $limit
               """.map(toPhoto).list.apply()
          Right(Page(photos, page, limit, total().toOption))
        } catch {
          case e: Exception ⇒
            logger.error("Unable to get photo")
            Left(DatabaseServiceError(e.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒
        logger.error(s"Error whilst retrieving photos", e)
        Left(DatabaseServiceError(e.getMessage))
    }

  private def total(): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          sql"SELECT COUNT(id) as total FROM common_photo"
            .map(rs ⇒ Right(rs.int("total")))
            .single
            .apply
            .getOrElse(Left(EmptyResultSet("Could not get photos")))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒ Left(DatabaseServiceError(e.getMessage))
    }

  def create(
      imageId: Option[String],
      name: String,
      path: Option[String],
      caption: String,
      createdAt: Timestamp,
      updatedAt: Option[Timestamp],
      deletedAt: Option[Timestamp],
      published: Boolean,
      cloudImage: Option[String],
      owner: Long): RepositoryResponse[Long] =
    try {
      writeTransaction(3, "Could not save new photo") { implicit session ⇒
        try {
          val id = sql"""INSERT INTO common_photo(
               image_id,
               `name`, 
               path, 
               caption, 
               created_at, 
               updated_at, 
               deleted_at, 
               published, 
               cloud_image, 
               owner)
            VALUES (
            $imageId,
            $name,
            $path,
            $caption,
            $createdAt,
            $updatedAt,
            $deletedAt,
            $published,
            $cloudImage,
            $owner
            )""".updateAndReturnGeneratedKey.apply()

          Right(id)
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒ Left(DatabaseServiceError(e.getMessage))
    }

  def update(photo: Photo): RepositoryResponse[Long] =
    try {
      writeTransaction(3, "Could not update photo") { implicit session ⇒
        try {
          val update =
            sql"""
           UPDATE common_photo 
           SET `name` = ${photo.name},
           path = ${photo.path},
           caption = ${photo.caption},
           created_at = ${photo.createdAt},
           updated_at = ${photo.updatedAt},
           deleted_at = ${photo.deletedAt},
           published = ${photo.published},
           cloud_image = ${photo.cloudImage},
           owner = ${photo.ownerId}
           WHERE id = ${photo.id}
         """.update.apply()
          if (update > 0) Right(photo.id)
          else Left(DatabaseServiceError(s"Could not update photo with id ${photo.id}"))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case e: Exception ⇒ Left(DatabaseServiceError(e.getMessage))
    }

  private val defaultSelect = {
    sqls"""
        SELECT
        p.id,
        p.image_id,
        p.name,
        p.path,
        p.caption,
        p.created_at,
        p.updated_at,
        p.deleted_at,
        p.published,
        p.cloud_image,
        p.owner,
        (SELECT COUNT(c.comment_id) FROM common_comment AS c WHERE c.image_id = p.id) AS comment_count,
        (SELECT COUNT(f.id) FROM common_favourite AS f WHERE f.photo_id = p.id) AS favourite_count
      FROM common_photo AS p
      """
  }

  private def selectPublished(publishedOnly: Option[Boolean], joiner: String = "WHERE") =
    publishedOnly
      .map { p ⇒
        val j = joiner match {
          case "AND" ⇒ sqls"AND"
          case _ ⇒ sqls"WHERE"
        }
        sqls" $j p.published = $p"
      }
      .getOrElse(sqls"")

  private val defaultOrdering = sqls"ORDER BY created_at DESC"

  private def toPhoto(res: WrappedResultSet) =
    Photo(
      res.int("id"),
      res.stringOpt("image_id"),
      res.string("name"),
      res.stringOpt("path"),
      res.string("caption"),
      res.timestamp("created_at"),
      res.timestampOpt("updated_at"),
      res.int("owner"),
      res.boolean("published"),
      res.timestampOpt("deleted_at"),
      res.stringOpt("cloud_image"),
      res.int("comment_count"),
      res.int("favourite_count")
    )
}
