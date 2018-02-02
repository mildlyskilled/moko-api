package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.{Favourite, Photo, User}
import com.mokocharlie.domain.common.MokoCharlieServiceError.DatabaseServiceError
import com.mokocharlie.domain.{Page, Password}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

trait FavouriteRepository {
  def favouritesByPhotoId(id: Long, page: Int, limit: Int): RepositoryResponse[Page[Favourite]]

  def addFavourite(favourite: Favourite): RepositoryResponse[Long]
}

class DBFavouriteRepository(override val config: Config)
    extends FavouriteRepository
    with JdbcRepository
    with StrictLogging {

  def favouritesByPhotoId(id: Long, page: Int, limit: Int): RepositoryResponse[Page[Favourite]] =
    readOnlyTransaction { implicit session ⇒
      try {
        val faves =
          sql"""
              $defaultSelect
        """
            .map(toFavourite)
            .list
            .apply()
        Right(Page(faves, page, limit, total()))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def addFavourite(favourite: Favourite): RepositoryResponse[Long] =
    writeTransaction(3, "Could not add to favourites") { implicit session ⇒
      try{
        val id = sql"""INSERT INTO common_favourite (photo_id, user_id, created_at)
         VALUES(${favourite.photo.id}, ${favourite.user.id}, ${favourite.createdAt})
       """
          .updateAndReturnGeneratedKey()
          .apply()
        Right(id)
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }

  }

  def findFavouritesByUserAndImage(imageID: Long, userID: Long): RepositoryResponse[Favourite] = ???

  private val defaultSelect: SQLSyntax =
    sqls"""
          | SELECT
          | f.id,
          | user_id,
          | created_at,
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
          |	p.published AS photo_published,
          | u.id AS user_id,
          | u.password,
          | u.last_login,
          | u.is_superuser,
          | u.email,
          | u.first_name,
          | u.last_name,
          | u.is_staff,
          | u.is_active,
          | u.date_joined
          | FROM common_favourite AS f
          | LEFT JOIN common_photo AS p
        """.stripMargin

  private def total(): Option[Int] =
    readOnlyTransaction { implicit session ⇒
    sql"SELECT COUNT(id) AS total FROM common_favourite".map(rs ⇒ rs.int("total")).single.apply()
  }

  private def toFavourite(rs: WrappedResultSet): Favourite = {
    val photo = Photo(
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

    val user = User(
      id = rs.long("user_id"),
      password = Password(rs.string("password")),
      lastLogin = rs.timestamp("last_login"),
      isSuperuser = rs.boolean("is_superuser"),
      email = rs.string("email"),
      firstName = rs.string("first_name"),
      lastName = rs.string("last_name"),
      isStaff = rs.boolean("is_staff"),
      isActive = rs.boolean("is_active"),
      dateJoined = rs.timestamp("date_joined")
    )

    Favourite(
      id = rs.long("id"),
      photo = photo,
      user = user,
      createdAt = rs.timestamp("created_at")
    )
  }
}
