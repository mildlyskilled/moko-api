package com.mokocharlie.repository

import java.sql.Timestamp

import com.mokocharlie.model.{Album, Page, Photo}
import com.mokocharlie.connection.Database
import slick.lifted.TableQuery
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future


trait AlbumRepository extends Database {

  class AlbumTable(tag: Tag) extends Table[Album](tag, "common_album") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def albumId = column[Option[Long]]("album_id")

    def label = column[String]("label")

    def description = column[String]("description")

    def coverId = column[Option[Long]]("cover_id")

    def createdAt = column[Timestamp]("created_at")

    def updatedAt = column[Option[Timestamp]]("updated_at")

    def published = column[Boolean]("published", O.Default(false))

    def featured = column[Boolean]("featured", O.Default(false))

    def * = (
      id,
      albumId,
      label,
      description,
      coverId,
      createdAt,
      updatedAt,
      published,
      featured
      ) <>((Album.apply _).tupled, Album.unapply)

  }

  val albums = TableQuery[AlbumTable]


  object AlbumDAO extends AlbumPhotoRepository with PhotoRepository {

    def list(page: Int, limit: Int, exclude: Seq[Long] = Seq()): Future[Page[Album]] = {
      val offset = limit * (page - 1)
      val select = albums.filter(_.published).sortBy(_.createdAt.desc.nullsFirst)

      val query = {
        if (exclude.isEmpty) select
        else select.filterNot(_.id inSet exclude)
      }

      for {
        total <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        albums <- db.run(query.drop(offset).take(limit).result)
      } yield Page(albums, page, limit, total.head)
    }


    def findAlbumByID(albumID: Long): Future[Option[Album]] = {
      db.run(albums.filter(_.id === albumID).result.headOption)
    }

    def getAlbumCoverByAlbumID(albumID: Long): Future[Option[Photo]] = {
      findAlbumByID(albumID).flatMap { coverOpt: Option[Album] =>
        coverOpt match {
          case Some(cover: Album) =>
            db.run(photos.filter(_.id === cover.coverId).result.headOption)
          case None =>
            Future.successful(None)
        }
      }
    }

    def getAlbumPhotos(albumID: Long, page: Int = 1, limit: Int = 10): Future[Page[Photo]] = {
      val offset = limit * (page - 1)
      val photoJoin = for {
        p <- photos
        pa <- photoAlbums.filter(_.albumID === albumID) if p.id === pa.photoID
      } yield p

      for {
        total <- db.run(photoJoin.length.result)
        photos <- db.run(photoJoin.result)
      } yield Page(photos, page, offset, total)
    }

    def getFeaturedAlbums(page: Int = 1, limit: Int = 10): Future[Page[Album]] = {
      val offset = limit * (page - 1)
      val query = albums.filter(_.featured).sortBy(_.createdAt.desc.nullsFirst)
      for {
        total <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        albums <- db.run(query.drop(offset).take(limit).result)
      } yield Page(albums, page, offset, total.head)
    }
  }

}