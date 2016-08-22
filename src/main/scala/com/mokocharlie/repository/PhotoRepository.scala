package com.mokocharlie.repository

import java.sql.Timestamp

import com.mokocharlie.connection.Database
import slick.driver.MySQLDriver.api._
import com.mokocharlie.model.{Page, Photo}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PhotoRepository extends Database with UserRepository {

  class PhotoTable(tag: Tag) extends Table[Photo](tag, "common_photo") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def imageID = column[String]("image_id")

    def name = column[String]("name")

    def path = column[Option[String]]("path")

    def caption = column[String]("caption")

    def createdAt = column[Timestamp]("created_at")

    def updatedAt = column[Timestamp]("updated_at")

    def ownerId = column[Long]("owner")

    def published = column[Boolean]("published")

    def deletedAt = column[Option[Timestamp]]("deleted_at")

    def cloudImage = column[Option[String]]("cloud_image")

    def * = (id,
      imageID,
      name,
      path,
      caption,
      createdAt,
      updatedAt,
      ownerId,
      published,
      deletedAt,
      cloudImage
      ) <> ((Photo.apply _).tupled, Photo.unapply)

    // foreign key lookup for user repository
    def ownerFk = foreignKey("owner_fk", ownerId, users)(_.id)
  }

  val photos = TableQuery[PhotoTable]

  object PhotoDAO extends AlbumPhotoRepository {

    def list(page: Int, limit: Int, exclude: Seq[Long] = Seq()): Future[Page[Photo]] = {
      val offset = limit * (page - 1)
      val select = photos.filter(_.published).filter(_.deletedAt.isEmpty).sortBy(_.createdAt.desc.nullsFirst)

      val query = {
        if (exclude.isEmpty) select
        else select.filterNot(_.id inSet exclude)
      }

      for {
        total <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        photos <- db.run(query.drop(offset).take(limit).result)
      } yield Page(photos, page, limit, total.headOption)
    }

    def findPhotoByImageID(imageID: String): Future[Option[Photo]] = {
      db.run(photos.filter(_.imageID === imageID).result.headOption)
    }

    def findPhotoByID(id: Long): Future[Option[Photo]] = {
      db.run(photos.filter(_.id === id).result.headOption)
    }

    def findPhotosByUserId(userId: Long, page: Int, limit: Int): Future[Page[Photo]] = {
      val query = photos.filter(_.ownerId === userId)
      for {
        total <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        photos <- db.run(query.result)
      } yield Page(photos, page, limit, total.headOption)
    }

    def getPhotosByAlbumId(albumID: Long, page: Int = 1, limit: Int = 10): Future[Page[Photo]] = {
      val offset = limit * (page - 1)
      val photoJoin = for {
        p <- photos
        pa <- photoAlbums.filter(_.albumID === albumID) if p.id === pa.photoID
      } yield p

      for {
        total <- db.run(photoJoin.length.result)
        photos <- db.run(photoJoin.result)
      } yield Page(photos, page, offset, Some(total))
    }

  }

}

