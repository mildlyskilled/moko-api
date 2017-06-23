package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.connection.Database
import com.mokocharlie.domain.{Documentary, Page}
import com.mokocharlie.model.Page
import slick.jdbc.MySQLProfile.api._
import slick.sql.SqlProfile.ColumnOption.NotNull

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DocumentaryRepository extends Database {

  class DocumentaryTable(tag: Tag) extends Table[Documentary](tag, "common_documentary") {

    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def description = column[String]("description")

    def status = column[String]("status", O.Length(11), O.Default("pending"), NotNull)

    def createdAt = column[Timestamp]("created_at", O.Default(new Timestamp(System.currentTimeMillis())))

    def updatedAt = column[Timestamp]("updated_at")

    def * = (id, description, status, createdAt, updatedAt) <>((Documentary.apply _).tupled, Documentary.unapply)
  }

  lazy val documentaries = TableQuery[DocumentaryTable]

  object DocumentaryDAO {
    def list(page: Int, limit: Int): Future[Page[Documentary]] = {
      val offset = limit * (page - 1)
      val query = documentaries

      for {
        total <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        documentaries <- db.run(query.drop(offset).take(limit).result)
      } yield Page(documentaries, page, offset, total.headOption)
    }

    def findDocumentaryByID(id: Long): Future[Option[Documentary]] = {
      db.run(documentaries.filter(_.id === id).result.headOption)
    }
  }

}
