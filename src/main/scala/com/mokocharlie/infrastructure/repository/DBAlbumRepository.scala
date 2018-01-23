package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable.Seq
import scalikejdbc._

trait AlbumRepository {
  def list(
      page: Int,
      limit: Int,
      exclude: Seq[Long] = Seq.empty,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]]

  def collectionAlbums(
      collectionID: Long,
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]]

  def albumById(albumID: Long): RepositoryResponse[Album]

  def featuredAlbums(
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]]

  def create(
      label: String,
      description: String,
      createdAt: Timestamp,
      coverImageId: Option[Long],
      published: Boolean,
      featured: Boolean): RepositoryResponse[Long]

  def update(album: Album): RepositoryResponse[Long]

  def savePhotosToAlbum(albumId: Long, photos: Seq[Long]): RepositoryResponse[Unit]

  def total(): Option[Int]
}

class DBAlbumRepository(override val config: Config, photoRepository: DBPhotoRepository)
    extends AlbumRepository
    with JdbcRepository
    with RepoUtils
    with StrictLogging {

  def list(
      page: Int,
      limit: Int,
      exclude: Seq[Long] = Seq.empty,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val albums =
          sql"""
            ${defaultSelect(publishedOnly)}
            $defaultOrdering
            LIMIT ${dbPage(page)}, ${offset(page, limit)}
           """
            .map(toAlbum)
            .list
            .apply()

        if (albums.nonEmpty) Right(Page(albums, page, limit, total()))
        else Left(EmptyResultSet("Did not find any albums"))

      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def collectionAlbums(
      collectionID: Long,
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val albums =
          sql"""
            ${defaultSelect(publishedOnly)}
            LEFT JOIN common_collection_albums AS cab
            ON cab.album_id = a.id
            WHERE cab.collection_id = $collectionID
            LIMIT ${dbPage(page)}, ${offset(page, limit)}
            $defaultOrdering
           """.map(toAlbum).list.apply()
        if (albums.nonEmpty) {
          Right(Page(albums, page, limit, total()))
        }
        else
          Left(EmptyResultSet(s"Did not find any albums with the given collectionID $collectionID"))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def albumById(albumID: Long): RepositoryResponse[Album] =
    readOnlyTransaction { implicit session ⇒
      try {
        sql"""
            ${defaultSelect()}
           WHERE a.id = $albumID
           """
          .map(toAlbum)
          .single
          .apply()
          .map(a ⇒ Right(a))
          .getOrElse(Left(EmptyResultSet(s"Could not find album with given id: $albumID")))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def featuredAlbums(
      page: Int = 1,
      limit: Int = 10,
      publishedOnly: Option[Boolean] = Some(true)): RepositoryResponse[Page[Album]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val albums =
          sql"""
            ${defaultSelect()}
            WHERE featured = 1
           LIMIT ${dbPage(page)}, ${offset(page, limit)}
           """.map(toAlbum).list.apply()

        if (albums.nonEmpty) {
          Right(Page(albums, page, limit, total()))
        }
        else Left(EmptyResultSet(s"Did not find any featured albums"))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  override def create(
      label: String,
      description: String,
      createdAt: Timestamp,
      coverImageId: Option[Long],
      published: Boolean,
      featured: Boolean): RepositoryResponse[Long] =
    writeTransaction(3, "Failed to save this album") { implicit session ⇒
      try {
        val id = sql"""
          INSERT INTO common_album (label, description, created_at, published, featured, cover_id)
          VALUES ($label, $description, $createdAt, $published, $featured, $coverImageId)""".updateAndReturnGeneratedKey
          .apply()
        Right(id)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def update(album: Album): RepositoryResponse[Long] =
    writeTransaction(3, s"Could not update album ${album.id}") { implicit session ⇒
      try {
        val update = sql"""
         UPDATE common_album 
          SET album_id = ${album.albumId},
          label = ${album.label},
           description = ${album.description},
           created_at = ${album.createdAt},
           updated_at = NOW(),
           published = ${album.published},
           featured = ${album.featured},
           cover_id = ${album.cover.map(_.id)}
           WHERE id = ${album.id}
        """.update.apply()
        if (update > 0) Right(album.id)
        else Left(DatabaseServiceError(s"Could not update album: ${album.id}"))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def savePhotosToAlbum(albumId: Long, photos: Seq[Long]): RepositoryResponse[Unit] =
    writeTransaction(1, "Could not update album") { implicit session ⇒
      }

  def total(): Option[Int] =
    readOnlyTransaction { implicit session ⇒
      sql"SELECT COUNT(id) AS total FROM common_album".map(rs ⇒ rs.int("total")).single.apply()
    }

  private def defaultSelect(publishedOnly: Option[Boolean] = Some(true)) = {
    val published = publishedOnly.map(p ⇒ sqls"AND a.published = $p").getOrElse(sqls"")
    sqls"""
          |SELECT
          |	a.id,
          |	a.album_id,
          |	a.label,
          |	a.description,
          |	a.created_at,
          |	a.updated_at,
          |	a.published,
          |	a.featured,
          |	a.cover_id,
          |	p.id AS photo_id,
          |	p.image_id AS legacy_id,
          |	p.name AS photo_name,
          |	p.caption AS photo_caption,
          |	p.created_at AS photo_created_at,
          |	p.deleted_at AS photo_deleted_at,
          |	p.`owner` AS photo_owner,
          |	p.path AS photo_path,
          |	p.`updated_at` AS photo_updated_at,
          |	p.cloud_image,
          |	p.published AS photo_published
          |FROM common_album AS a
          |LEFT JOIN common_photo p ON a.cover_id = p.id $published
       """.stripMargin
  }

  private val defaultOrdering = sqls"ORDER BY created_at DESC"

  private def toAlbum(rs: WrappedResultSet): Album = {
    val photo = rs.longOpt("cover_id").map { _ ⇒
      Photo(
        rs.int("photo_id"),
        rs.string("legacy_id"),
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

    Album(
      rs.int("id"),
      rs.longOpt("album_id"),
      rs.string("label"),
      rs.string("description"),
      photo,
      rs.timestamp("created_at"),
      rs.timestampOpt("updated_at"),
      rs.boolean("published"),
      rs.boolean("featured")
    )
  }
}
