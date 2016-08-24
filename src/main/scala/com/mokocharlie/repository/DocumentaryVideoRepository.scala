package com.mokocharlie.repository

import com.mokocharlie.connection.Database
import com.mokocharlie.model.DocumentaryVideo
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._

trait DocumentaryVideoRepository extends Database {

  class DocumentaryVideoTable(tag: Tag) extends Table[DocumentaryVideo](tag, "common_documentary_video") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def documentaryID = column[Long]("documentary_id")

    def videoID = column[Long]("video_id")

    def * = (id, documentaryID, videoID) <> ((DocumentaryVideo.apply _).tupled, DocumentaryVideo.unapply)
  }

  lazy val documentaryVideos = TableQuery[DocumentaryVideoTable]

}
