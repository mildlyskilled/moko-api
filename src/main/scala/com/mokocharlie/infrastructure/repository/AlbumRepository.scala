package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.MokoCharlieServiceError.DatabaseServiceError
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable.Seq
import scalikejdbc._


class AlbumRepository(override val config: Config)
  extends JdbcRepository
  with StrictLogging {

    def list(page: Int, limit: Int, exclude: Seq[Long] = Seq.empty): RepositoryResponse[Page[Album]] =
      readOnlyTransaction{implicit session ⇒
        try{
          val offset = (page * limit) + 1
          val albums = sql"""
            $defaultSelect
           LIMIT $offset, $limit
           """.map(toAlbum).list.apply()
          Right(Page(albums, page, limit, total()))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }

  def findAlbumByID(albumID: Long): RepositoryResponse[Option[Album]] = ???

  def getAlbumCoverByAlbumID(albumID: Long): RepositoryResponse[Option[Photo]] = ???

  def getFeaturedAlbums(page: Int = 1, limit: Int = 10): RepositoryResponse[Page[Album]] = ???

  def total(): Option[Int] =
    readOnlyTransaction { implicit session ⇒
      sql"SELECT COUNT(id) AS total FROM common_album".map(rs ⇒ rs.int("total")).single.apply()
    }

  private val defaultSelect =
    sqls"""
         SELECT
         id,
         album_id,
         label,
         description,
         created_at,
         updated_at,
         published,
         featured,
         cover_id
         FROM common_album
       """

  private def toAlbum(rs: WrappedResultSet): Album =
    Album(
      rs.int("id"),
      rs.longOpt("album_id"),
      rs.string("label"),
      rs.string("description"),
      rs.longOpt("cover_id"),
      rs.timestamp("created_at"),
      rs.timestampOpt("updated_at"),
      rs.boolean("published"),
      rs.boolean("featured")
    )
}