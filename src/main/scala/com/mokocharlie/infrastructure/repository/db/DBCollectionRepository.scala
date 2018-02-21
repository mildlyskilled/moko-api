package com.mokocharlie.infrastructure.repository.db

import com.mokocharlie.domain.MokoModel.{Album, Collection, Photo}
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.CollectionRepository
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import scalikejdbc._

import scala.collection.immutable.Seq

class DBCollectionRepository(override val config: Config)
  extends CollectionRepository
    with JdbcRepository
    with RepoUtils {

  def list(
            page: Int,
            limit: Int,
            publishedOnly: Option[Boolean] = None): RepositoryResponse[Page[Collection]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val list = sql"""
          $defaultSelect
          ${selectPublished(publishedOnly)}
          LIMIT ${offset(page, limit)}, $limit
        """
          .map(toCollection)
          .list()
          .apply()

        if (list.isEmpty) Left(EmptyResultSet("Could not find any collections"))
        Right(Page(list, page, limit, total().toOption))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def collectionById(id: Long): RepositoryResponse[Collection] =
    readOnlyTransaction { implicit session ⇒
      try {
        sql"""
            $defaultSelect
            WHERE c.id = $id
        """
          .map(toCollection)
          .single
          .apply()
          .map(c ⇒ Right(c))
          .getOrElse(Left(EmptyResultSet(s"Could not find collection with id: $id")))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def featuredCollections(page: Int = 1, limit: Int = 10): RepositoryResponse[Page[Collection]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val collections = sql"""
            $defaultSelect
            WHERE c.featured = 1
            LIMIT ${offset(page, limit)}, $limit
        """
          .map(toCollection)
          .list
          .apply()
        Right(Page(collections, page, limit, total().toOption))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def create(collection: Collection): RepositoryResponse[Long] =
    writeTransaction(3, "Could not create colection") { implicit session ⇒
      try {
        val id =
          sql"""
           INSERT INTO common_collection (
           `name`,
           featured,
           published,
           created_at,
           updated_at,
           description,
           cover_album_id
           )
           VALUES (
           ${collection.name},
           ${collection.featured},
           ${collection.published},
           ${collection.createdAt},
           ${collection.updatedAt},
           ${collection.description},
           ${collection.coverAlbum.map(_.id)}

           )
         """.updateAndReturnGeneratedKey()
            .apply()
        Right(id)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }

    }

  def update(collection: Collection): RepositoryResponse[Long] =
    writeTransaction(3, s"Could not update collection with id: ${collection.id}") {
      implicit session ⇒
        try {
          val res = sql"""
                UPDATE common_collection SET
                `name` = ${collection.name},
                featured = ${collection.featured},
                published = ${collection.published},
                created_at = ${collection.createdAt},
                updated_at = ${collection.updatedAt},
                description = ${collection.description},
                cover_album_id = ${collection.coverAlbum.map(_.id)}
                WHERE id = ${collection.id}
             """.update
            .apply()
          if (res > 0) Right(collection.id)
          else Left(DatabaseServiceError(s"Could not update album: ${collection.id}"))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
    }

  def saveAlbumToCollection(collectionId: Long, albumIds: Seq[Long]): RepositoryResponse[Seq[Int]] =
    writeTransaction(3, "Could not save albums to collection") { implicit session ⇒
      try {
        val inserts = albumIds.map(id ⇒ Seq(collectionId, id))
        val ids = sql"""
           INSERT INTO common_collection_albums(collection_id, album_id) VALUES (?, ?)
         """
          .batch(inserts: _*)
          .apply()
        Right(ids)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def removeAlbumFromCollection(collectionId: Long, albums: Seq[Long]): RepositoryResponse[Seq[Int]] =
    writeTransaction(3, "Could not remove albums from collection"){implicit session ⇒
      try {
        val deletes = albums.map(id ⇒ Seq(collectionId, id))
        val res =
          sql"""
            DELETE FROM common_collection_albums
            WHERE collection_id = ? AND album_id = ?
      """.batch(deletes: _*).apply()
        Right(res)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }


  private def total(): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        sql"SELECT COUNT(id) as total FROM common_collection"
          .map(rs ⇒ Right(rs.int("total")))
          .single
          .apply()
          .getOrElse(Left(EmptyResultSet("Could not get any collections")))
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  private val defaultSelect: SQLSyntax =
    sqls"""
         SELECT
          | c.id AS collection_id,
          | c.name,
          | c.featured,
          | c.created_at,
          | c.updated_at,
          | c.description,
          | c.published,
          | c.cover_album_id,
          | a.label AS album_label,
          | a.album_id AS legacy_album_id,
          | a.description AS album_description,
          | a.published AS album_published,
          | a.featured AS album_featured,
          | a.created_at as album_created_at,
          | a.updated_at AS album_updated_at,
          | a.cover_id AS album_cover,
          |	p.id AS photo_id,
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
          | FROM common_collection AS c
          | LEFT JOIN common_album AS a ON c.cover_album_id = a.id
          | LEFT JOIN common_photo AS p ON p.id = a.cover_id
        """.stripMargin

  private def selectPublished(publishedOnly: Option[Boolean], joiner: String = "WHERE") =
    publishedOnly
      .map { p ⇒
        val j = joiner match {
          case "AND" ⇒ sqls"AND"
          case _ ⇒ sqls"WHERE"
        }
        sqls"$j c.published = $p"
      }
      .getOrElse(sqls"")

  private def toCollection(rs: WrappedResultSet): Collection = {
    val coverAlbum = rs.longOpt("cover_album_id").map{ coverAlbumId ⇒

      val cover = rs.longOpt("album_cover"). map{ _ ⇒
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

      Album(
        id = coverAlbumId,
        albumId = rs.longOpt("legacy_album_id"),
        label = rs.string("album_label"),
        description = rs.string("album_description"),
        published = rs.boolean("album_published"),
        featured = rs.boolean("album_featured"),
        createdAt = rs.timestamp("album_created_at"),
        updatedAt = rs.timestampOpt("album_updated_at"),
        cover = cover
      )
    }

    Collection(
      id = rs.long("collection_id"),
      name = rs.string("name"),
      featured = rs.boolean("featured"),
      published = rs.boolean("published"),
      createdAt = rs.timestamp("created_at"),
      updatedAt = rs.timestamp("updated_at"),
      description = rs.string("description"),
      coverAlbum  = coverAlbum
    )
  }
}
