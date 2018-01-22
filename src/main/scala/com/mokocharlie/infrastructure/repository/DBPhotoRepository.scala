package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.Photo
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.DatabaseServiceError
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

trait PhotoRepository {
  def list(
      page: Int,
      limit: Int,
      exclude: Seq[Long] = Seq.empty,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]]

  def photoById(imageID: String): RepositoryResponse[Option[Photo]]

  def photoById(id: Long): RepositoryResponse[Option[Photo]]

  def photosByUserId(
      userId: Long,
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean] = None): RepositoryResponse[Page[Photo]]

  def photosByAlbumId(
      albumID: Long,
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]]

  def update(photo: Photo): RepositoryResponse[Long]

  def total(): Option[Int]

}

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
    readOnlyTransaction { implicit session ⇒
      try {
        val photos =
          sql"""
              ${defaultSelect(publishedOnly)}
              $defaultOrdering
              LIMIT ${offset(page, limit)}, $limit

        """.map(toPhoto)
            .list()
            .apply()

        Right(Page(photos, page, limit, total()))
      } catch {
        case e: Exception ⇒
          logger.error(s"Error whilst retrieving photos", e)
          Left(DatabaseServiceError(e.getMessage))
      }
    }

  def photoById(imageID: String): RepositoryResponse[Option[Photo]] =
    readOnlyTransaction { implicit session ⇒
      try {
        Right {
          sql"""
               ${defaultSelect()}
              AND image_id = $imageID
             """.map(toPhoto).single.apply()
        }
      } catch {
        case e: Exception ⇒
          logger.error("Unable to get photo")
          Left(DatabaseServiceError(e.getMessage))
      }
    }

  def photoById(id: Long): RepositoryResponse[Option[Photo]] =
    readOnlyTransaction { implicit session ⇒
      try {
        Right {
          sql"""
               ${defaultSelect()}
              WHERE id = $id
             """.map(toPhoto).single.apply()
        }
      } catch {
        case e: Exception ⇒
          logger.error("Unable to get photo")
          Left(DatabaseServiceError(e.getMessage))
      }
    }

  def photosByUserId(
      userId: Long,
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean] = None): RepositoryResponse[Page[Photo]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val photos = sql"""
               ${defaultSelect(publishedOnly)}
               $defaultOrdering
              WHERE ownder = $userId
              LIMIT , $limit
             """.map(toPhoto).list.apply()
        Right(Page(photos, page, limit, total()))
      } catch {
        case e: Exception ⇒
          logger.error("Unable to get photo")
          Left(DatabaseServiceError(e.getMessage))
      }
    }

  def photosByAlbumId(
      albumID: Long,
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val photos =
          sql"""
            ${defaultSelect()}
            LEFT JOIN common_photo_album AS cab
            ON p.id = cab.photo_id
            WHERE cab.album_id = $albumID
            LIMIT ${offset(page, limit)}, $limit
               """.map(toPhoto).list.apply()
        Right(Page(photos, page, limit, total()))
      } catch {
        case e: Exception ⇒
          logger.error("Unable to get photo")
          Left(DatabaseServiceError(e.getMessage))
      }
    }

  def total(): Option[Int] =
    readOnlyTransaction { implicit session ⇒
      sql"SELECT COUNT(id) as total FROM common_photo".map(rs ⇒ rs.int("total")).single.apply()
    }

  def create(
      name: String,
      path: Option[String],
      caption: String,
      createdAt: Timestamp,
      updatedAt: Option[Timestamp],
      deletedAt: Option[Timestamp],
      published: Boolean,
      cloudImage: Option[String],
      owner: Long): RepositoryResponse[Long] =
    writeTransaction(3, "Could not save new photo") { implicit session ⇒
      try {
        val id = sql"""INSERT INTO common_photo(
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

  def update(photo: Photo): RepositoryResponse[Long] =
    writeTransaction(3, "Could not update photo") { implicit session ⇒
      try {
        val update = sql"""
           UPDATE common_photo 
           SET `name` = ${photo.name},
           path = ${photo.path},
           caption = ${photo.caption},
           created_at = ${photo.createdAt},
           updated_at = NOW() ,
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

  private def defaultSelect(publishedOnly: Option[Boolean] = None) = {
    val publish = publishedOnly.map(p ⇒ sqls"WHERE published = $p").getOrElse(sqls"WHERE 1")
    sqls"""
        SELECT
        id,
        image_id,
        name,
        path,
        caption,
        created_at,
        updated_at,
        deleted_at,
        published,
        cloud_image,
        owner
      FROM common_photo AS p
      $publish
      """
  }

  private val defaultOrdering = sqls"ORDER BY created_at DESC"

  private def toPhoto(res: WrappedResultSet) =
    Photo(
      res.int("id"),
      res.string("image_id"),
      res.string("name"),
      res.stringOpt("path"),
      res.string("caption"),
      res.timestamp("created_at"),
      res.timestampOpt("updated_at"),
      res.int("owner"),
      res.boolean("published"),
      res.timestampOpt("deleted_at"),
      res.stringOpt("cloud_image")
    )
}
