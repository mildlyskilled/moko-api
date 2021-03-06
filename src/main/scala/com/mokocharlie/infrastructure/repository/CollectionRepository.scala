package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.Page
import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.common.MokoCharlieServiceError.DatabaseServiceError
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import scalikejdbc._


class CollectionRepository(override val config: Config) extends JdbcRepository with RepoUtils {

  def list(page: Int, limit: Int): RepositoryResponse[Page[Collection]] = ???

  def findCollectionById(id: Long): RepositoryResponse[Option[Collection]] =
    readOnlyTransaction{ implicit session ⇒
      try{
        val collection = sql"""
            $defaultSelect
            WHERE id = $id
        """.map(toCollection)
          .single
          .apply()
        Right(collection)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def getFeaturedCollections(page: Int = 1, limit: Int = 10): RepositoryResponse[Page[Collection]] =
    readOnlyTransaction{ implicit  session ⇒
      try{
        val collections = sql"""
            $defaultSelect
            WHERE featured = 1
            LIMT ${offset(page, limit)}, $limit
        """.map(toCollection)
          .list
          .apply()
        Right(Page(collections, page, limit, total()))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def total(): Option[Int] = readOnlyTransaction { implicit session ⇒
    sql"SELECT COUNT(id) as total FROM common_collection".map(rs ⇒ rs.int("total")).single.apply()
  }

  private def toCollection(rs: WrappedResultSet): Collection =
    Collection(
      rs.long("id"),
      rs.string("name"),
      rs.boolean("feattured"),
      rs.timestamp("created_at"),
      rs.timestamp("updated_at"),
      rs.string("description"),
      rs.long("cover_album_id")
    )

  private val defaultSelect: SQLSyntax =
      sqls"""
         SELECT
          id,
          name,
          featured,
          created_at,
          updated_at,
          descripion,
          cover_album_id
          FROM common_collection
        """

}
