package com.mokocharlie.repository

import java.sql.Timestamp

import com.mokocharlie.connection.Database
import com.mokocharlie.model.Favourite
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

trait FavouriteRepository extends Database {

  class FavouriteTable(tag: Tag) extends Table[Favourite](tag, "common_favourite") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def photoID = column[Long]("photo_id")

    def userID = column[Long]("user_id")

    def createdAt = column[Timestamp]("created_at")

    def * = (id, photoID, userID, createdAt) <>((Favourite.apply _).tupled, Favourite.unapply)
  }

  val favourites = TableQuery[FavouriteTable]

  object FavouriteDAO {
    def findFavouritesByImageID(imageID: Long): Future[Seq[Favourite]] = {
      val query = favourites filter (_.photoID === imageID)
      db.run(query.result)
    }

    def findFavouritesByUserAndImage(imageID: Long, userID: Long): Future[Option[Favourite]] = {
      val query = favourites filter (_.photoID === imageID) filter (_.userID === userID)
      db.run(query.result.headOption)
    }
  }

}
