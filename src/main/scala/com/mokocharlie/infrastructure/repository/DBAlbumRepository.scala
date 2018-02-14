package com.mokocharlie.infrastructure.repository

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

  def create(album: Album): RepositoryResponse[Long]

  def update(album: Album): RepositoryResponse[Long]

  def savePhotosToAlbum(albumId: Long, photoIds: Seq[Long]): RepositoryResponse[Seq[Int]]

  def removePhotosFromAlbum(albumId: Long, photoIds: Seq[Long]): RepositoryResponse[Seq[Int]]

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
    try {
      readOnlyTransaction { implicit session ⇒
        val albums =
          sql"""
            ${defaultSelect}
            ${selectPublished(publishedOnly, "WHERE")}
            $defaultOrdering
            LIMIT ${dbPage(page)}, ${rowCount(page, limit)}
           """
            .map(toAlbum)
            .list
            .apply()

        if (albums.nonEmpty) Right(Page(albums, page, limit, total()))
        else Left(EmptyResultSet("Did not find any albums"))
      }
    } catch {
      case ex: Exception ⇒
        logger.error(s"We got a database service error ${ex}")
        Left(DatabaseServiceError(ex.getMessage))
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
            ${defaultSelect}
            LEFT JOIN common_collection_albums AS cab
            ON cab.album_id = a.id
            WHERE cab.collection_id = $collectionID
            ${selectPublished(publishedOnly, "AND")}
            $defaultOrdering
            LIMIT ${dbPage(page)}, ${rowCount(page, limit)}
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
            ${defaultSelect}
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
            ${defaultSelect}
            WHERE featured = 1
           LIMIT ${dbPage(page)}, ${rowCount(page, limit)}
           """.map(toAlbum).list.apply()

        if (albums.nonEmpty) {
          Right(Page(albums, page, limit, total()))
        }
        else Left(EmptyResultSet(s"Did not find any featured albums"))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  override def create(album: Album): RepositoryResponse[Long] =
    writeTransaction(3, "Failed to save this album") { implicit session ⇒
      try {
        val id = sql"""
          INSERT INTO common_album (label, album_id, description, created_at, updated_at, published, featured, cover_id)
          VALUES (
          ${album.label},
          ${album.albumId},
          ${album.description},
          ${album.createdAt},
          ${album.updatedAt},
          ${album.published},
          ${album.featured},
          ${album.cover.map(_.id)})""".updateAndReturnGeneratedKey
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
           updated_at = ${album.updatedAt},
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

  def savePhotosToAlbum(albumId: Long, photoIds: Seq[Long]): RepositoryResponse[Seq[Int]] =
    writeTransaction(1, "Could not update album") { implicit session ⇒
      try {
        val inserts = photoIds.map(id ⇒ Seq(albumId, id))
        val res = sql"""INSERT INTO common_photo_albums (album_id, photo_id)
           VALUES (?, ?)
         """
          .batch(inserts: _*)
          .apply()
        Right(res)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }

    }

  def removePhotosFromAlbum(albumId: Long, photoIds: Seq[Long]): RepositoryResponse[Seq[Int]] =
    writeTransaction(3, s"Could not remove photos from album $albumId") { implicit session ⇒
      try {
        val deletes = photoIds.map(id ⇒ Seq(albumId, id))
        val res = sql"""
          DELETE FROM common_photo_albums
          WHERE photo_id = ? AND album_id = ? """
          .batch(deletes: _*)
          .apply()
        Right(res)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def total(): Option[Int] =
    readOnlyTransaction { implicit session ⇒
      sql"SELECT COUNT(id) AS total FROM common_album".map(rs ⇒ rs.int("total")).single.apply()
    }

  private val defaultSelect = {
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
          |LEFT JOIN common_photo p ON a.cover_id = p.id
       """.stripMargin
  }

  private val defaultOrdering = sqls"ORDER BY created_at DESC"

  private def toAlbum(rs: WrappedResultSet): Album = {
    val photo = rs.longOpt("cover_id").map { _ ⇒
      Photo(
        rs.int("photo_id"),
        rs.stringOpt("legacy_id"),
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

  private def selectPublished(publishedOnly: Option[Boolean], joiner: String = "WHERE") =
    publishedOnly
      .map { p ⇒
        val j = joiner match {
          case "AND" ⇒ sqls"AND"
          case _ ⇒ sqls"WHERE"
        }
        sqls" $j a.published = $p"
      }
      .getOrElse(sqls"")
}
