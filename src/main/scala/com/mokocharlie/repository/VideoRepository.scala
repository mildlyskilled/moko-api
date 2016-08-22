package com.mokocharlie.repository

import com.mokocharlie.connection.Database
import com.mokocharlie.model.{Page, Video}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._


trait VideoRepository extends Database {

  class VideoTable(tag: Tag) extends Table[Video](tag, "common_video") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def externalId = column[String]("external_id")

    def externalSource = column[String]("external_source")

    def * = (id, externalId, externalSource) <>((Video.apply _).tupled, Video.unapply)
  }

  lazy val videos = TableQuery[VideoTable]

  object VideoDAO {

    def findVideoByID(id: Long): Future[Option[Video]] = {
      db.run(videos.filter(_.id === id).result.headOption)
    }


    def list(page: Int, limit: Int): Future[Page[Video]] = {
      val offset = limit * (page - 1)
      val query = videos

      for {
        total <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        items <- db.run(query.drop(offset).take(limit).result)
      } yield Page(items, page, offset, total.headOption)
    }

  }

}
