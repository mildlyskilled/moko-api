package com.mokocharlie.infrastructure.repository.db

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.{Favourite, Photo, User}
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.domain.{Page, Password}
import com.mokocharlie.infrastructure.repository.FavouriteRepository
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

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
             WHERE p.id = $id
        """.map(toFavourite)
            .list
            .apply()
        Right(Page(faves, page, limit, Some(faves.size)))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def addFavourite(userId: Long, photoId: Long, createdAt: Timestamp): RepositoryResponse[Long] =
    writeTransaction(3, "Could not add to favourites") { implicit session ⇒
      try {
        sql"COUNT(id) AS count FROM common_favourite WHERE user_id = $userId AND photo_id = $photoId"
          .map(rs ⇒ rs.int("count"))
          .single
          .apply().collect{
            case count if count > 0 ⇒ Left(DatabaseServiceError("This photo already exists in user favourites"))
          }.getOrElse {
            Right {
              sql"""INSERT INTO common_favourite (photo_id, user_id, created_at)
         VALUES($photoId, $userId, $createdAt)
       """.updateAndReturnGeneratedKey()
                .apply()
            }
          }

      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }

    }

  def findFavouritesByUserAndImage(imageID: Long, userID: Long): RepositoryResponse[Favourite] = ???

  private val defaultSelect: SQLSyntax =
    sqls"""
          | SELECT
          | f.id,
          | f.photo_id,
          | f.user_id,
          | f.created_at,
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
          | u.date_joined,
          | (SELECT COUNT(c.comment_id) FROM common_comment AS c WHERE c.image_id = p.id AND c.comment_approved) AS comment_count,
          | (SELECT COUNT(f.id) FROM common_favourite AS f WHERE f.photo_id = p.id) AS favourite_count
          | FROM common_favourite AS f
          | INNER JOIN common_photo AS p ON p.id = f.photo_id
          | INNER JOIN common_mokouser AS u ON f.user_id = u.id
        """.stripMargin

  private def total(wherePredicate: SQLSyntax): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        sql"SELECT COUNT(id) AS total FROM common_favourite $wherePredicate"
          .map(rs ⇒ Right(rs.int("total")))
          .single
          .apply
          .getOrElse(Left(EmptyResultSet("Could not get favourites")))
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
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
      rs.stringOpt("cloud_image"),
      rs.int("comment_count"),
      rs.int("favourite_count")
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
