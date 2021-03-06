package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Photo
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, UnknownError}
import com.mokocharlie.domain.common.ServiceResponse.{RepositoryResponse, ServiceResponse}
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

class PhotoRepository(override val config: Config)
  extends JdbcRepository
  with RepoUtils
  with StrictLogging {

    def list(page: Int, limit: Int, exclude: Seq[Long] = Seq(), publishedOnly:Option[Boolean] = Some(true)): RepositoryResponse[Page[Photo]] =
      readOnlyTransaction { implicit session ⇒
        try {
          val photos =
            sql"""
              ${defaultSelect(publishedOnly)}
              $defaultOrdering
              LIMIT ${offset(page, limit)}, $limit

        """
              .map(toPhoto).list().apply()

          Right(Page(photos, page, limit, total()))
        } catch {
          case e: Exception ⇒
            logger.error(s"Error whilst retrieving photos", e)
            Left(DatabaseServiceError(e.getMessage))
        }
      }

    def findPhotoByImageID(imageID: String): RepositoryResponse[Option[Photo]] =
      readOnlyTransaction { implicit session ⇒
        try {
          Right{
            sql"""
               ${defaultSelect()}
              AND image_id = $imageID
             """.map(toPhoto).single.apply()
          }
        } catch {
          case e: Exception ⇒ logger.error("Unable to get photo")
            Left(DatabaseServiceError(e.getMessage))
        }
      }

    def findPhotoByID(id: Long): RepositoryResponse[Option[Photo]] =
      readOnlyTransaction { implicit session ⇒
        try {
          Right{
            sql"""
               ${defaultSelect()}
              WHERE id = $id
             """.map(toPhoto).single.apply()
          }
        } catch {
          case e: Exception ⇒ logger.error("Unable to get photo")
            Left(DatabaseServiceError(e.getMessage))
        }
      }

    def findPhotosByUserId(userId: Long, page: Int, limit: Int, publishedOnly: Option[Boolean] = None): RepositoryResponse[Page[Photo]] =
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
          case e: Exception ⇒ logger.error("Unable to get photo")
            Left(DatabaseServiceError(e.getMessage))
        }
      }

  def getPhotosByAlbumId(
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
        case e: Exception ⇒ logger.error("Unable to get photo")
          Left(DatabaseServiceError(e.getMessage))
      }
    }

  def total(): Option[Int] =
    readOnlyTransaction { implicit session ⇒
      sql"SELECT COUNT(id) as total FROM common_photo".map(rs ⇒ rs.int("total")).single.apply()
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
      res.timestamp("updated_at"),
      res.int("owner"),
      res.boolean("published"),
      res.timestampOpt("deleted_at"),
      res.stringOpt("cloud_image"))
}
