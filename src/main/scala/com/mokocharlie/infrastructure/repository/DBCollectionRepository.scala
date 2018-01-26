package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.Page
import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import scalikejdbc._
import scala.collection.immutable.Seq

trait CollectionRepository {
  def list(
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): RepositoryResponse[Page[Collection]]

  def collectionById(id: Long): RepositoryResponse[Collection]

  def featuredCollections(page: Int = 1, limit: Int = 10): RepositoryResponse[Page[Collection]]

  def create(collection: Collection): RepositoryResponse[Long]

  def update(collection: Collection): RepositoryResponse[Long]

  def saveAlbumToCollection(collectionId: Long, albums: Seq[Long]): RepositoryResponse[Seq[Int]]

  def total(): Option[Int]
}

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
          LIMIT ${dbPage(page)}, ${rowCount(page, limit)}
        """
          .map(toCollection)
          .list()
          .apply()

        if (list.isEmpty) Left(EmptyResultSet("Could not find any collections"))
        Right(Page(list, page, rowCount(page, limit), Some(limit)))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def collectionById(id: Long): RepositoryResponse[Collection] =
    readOnlyTransaction { implicit session ⇒
      try {
        sql"""
            $defaultSelect
            WHERE id = $id
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
            WHERE featured = 1
            LIMT ${rowCount(page, limit)}, $limit
        """
          .map(toCollection)
          .list
          .apply()
        Right(Page(collections, page, limit, total()))
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
           ${collection.coverAlbumId}
           
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
                cover_album_id = ${collection.coverAlbumId}
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
  def total(): Option[Int] = readOnlyTransaction { implicit session ⇒
    sql"SELECT COUNT(id) as total FROM common_collection".map(rs ⇒ rs.int("total")).single.apply()
  }

  private def toCollection(rs: WrappedResultSet): Collection =
    Collection(
      id = rs.long("id"),
      name = rs.string("name"),
      featured = rs.boolean("featured"),
      published = rs.boolean("published"),
      createdAt = rs.timestamp("created_at"),
      updatedAt = rs.timestamp("updated_at"),
      description = rs.string("description"),
      coverAlbumId = rs.long("cover_album_id")
    )

  private val defaultSelect: SQLSyntax =
    sqls"""
         SELECT
          c.id,
          c.name,
          c.featured,
          c.created_at,
          c.updated_at,
          c.description,
          c.published,
          c.cover_album_id
          FROM common_collection AS c
        """

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

}
