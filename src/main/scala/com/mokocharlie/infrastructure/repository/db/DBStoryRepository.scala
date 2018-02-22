package com.mokocharlie.infrastructure.repository.db

import com.mokocharlie.domain.MokoModel.{Album, Photo, Story}
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.{MokoModel, Page}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.StoryRepository
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import scalikejdbc._

class DBStoryRepository(override val config: Config)
    extends StoryRepository
    with JdbcRepository
    with RepoUtils {

  override def list(
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): RepositoryResponse[Page[MokoModel.Story]] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          val stories = sql"""
             $defaultSelect
             ${selectPublished(publishedOnly)}
             LIMIT ${offset(page, limit)}, $limit
            """
            .map(toStory)
            .list
            .apply()
          Right(Page(stories, page, offset(page, limit), total().toOption))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  override def storyById(id: Long): RepositoryResponse[MokoModel.Story] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          sql"$defaultSelect WHERE s.id = $id"
            .map(r ⇒ Right(toStory(r)))
            .single
            .apply
            .getOrElse(Left(EmptyResultSet(s"Could not find story with id: $id")))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  private val defaultSelect: SQLSyntax =
    sqls""" SELECT
          | s.id,
          | s.`name`,
          | s.description,
          | s.created_at,
          | s.published,
          | a.id AS album_id,
          | a.label AS album_label,
          | a.album_id AS legacy_album_id,
          | a.description AS album_description,
          | a.published AS album_published,
          | a.featured AS album_featured,
          | a.created_at as album_created_at,
          | a.updated_at AS album_updated_at,
          | a.cover_id AS album_cover,
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
          | FROM common_photostory AS s
          | LEFT JOIN common_album AS a ON a.id = s.album_id
          | LEFT JOIN common_photo AS p ON p.id = a.cover_id
        """.stripMargin

  private def toStory(rs: WrappedResultSet): Story = {
    val cover = rs.longOpt("album_cover").map { _ ⇒
      Photo(
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
    }

    val album = Album(
      id = rs.long("album_id"),
      albumId = rs.longOpt("legacy_album_id"),
      label = rs.string("album_label"),
      description = rs.string("album_description"),
      published = rs.boolean("album_published"),
      featured = rs.boolean("album_featured"),
      createdAt = rs.timestamp("album_created_at"),
      updatedAt = rs.timestampOpt("album_updated_at"),
      cover = cover
    )

    Story(
      id = rs.long("id"),
      name = rs.string("name"),
      description = rs.string("description"),
      published = rs.boolean("published"),
      createdAt = rs.timestamp("created_at"),
      album = album
    )
  }

  private def total(wherePredicate: SQLSyntax = sqls""): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        sql"SELECT COUNT(id) AS total FROM common_photostory $wherePredicate"
          .map(r ⇒ Right(r.int("total")))
          .single
          .apply
          .getOrElse(Left(DatabaseServiceError("Could not get total")))
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  private def selectPublished(publishedOnly: Option[Boolean], joiner: String = "WHERE") =
    publishedOnly
      .map { p ⇒
        val j = joiner match {
          case "AND" ⇒ sqls"AND"
          case _ ⇒ sqls"WHERE"
        }
        sqls"$j s.published = $p"
      }
      .getOrElse(sqls"")
}
