package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.connection.Database
import com.mokocharlie.domain.{Favourite, Page}
import com.mokocharlie.model.Page
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait FavouriteRepository extends Database {

  class FavouriteTable(tag: Tag) extends Table[Favourite](tag, "common_favourite") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def photoID = column[Long]("photo_id")

    def userID = column[Long]("user_id")

    def createdAt = column[Timestamp]("created_at")

    def * = (id, photoID, userID, createdAt) <> ((Favourite.apply _).tupled, Favourite.unapply)
  }

  val favourites = TableQuery[FavouriteTable]

  object FavouriteDAO {
    def findFavouritesByImageID(imageID: Long, page: Int, limit: Int): Future[Page[Favourite]] = {
      val offset = limit * page

      val query = favourites filter (_.photoID === imageID)

      for {
        totalFavourites <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        favourites <- db.run(query.result)
      } yield Page(favourites, page, offset, totalFavourites.headOption)
    }

    def findFavouritesByUserAndImage(imageID: Long, userID: Long): Future[Option[Favourite]] = {
      val query = favourites filter (_.photoID === imageID) filter (_.userID === userID)
      db.run(query.result.headOption)
    }
  }

}
