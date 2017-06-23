package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.connection.Database
import com.mokocharlie.domain.{Album, Collection, Page}
import com.mokocharlie.model.{Collection, Page}

import scala.concurrent.ExecutionContext.Implicits.global
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/**
  * Created by kwabena on 01/08/2016.
  */
trait CollectionRepository extends Database {

  class CollectionTable(tag: Tag) extends Table[Collection](tag, "common_collection") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def featured = column[Boolean]("featured")

    def createdAt = column[Timestamp]("created_at")

    def updatedAt = column[Timestamp]("updated_at")

    def description = column[String]("description")

    def coverAlbumId = column[Long]("cover_album_id")


    def * = (
      id,
      name,
      featured,
      createdAt,
      updatedAt,
      description,
      coverAlbumId
      ) <> ((Collection.apply _).tupled, Collection.unapply)

  }

  var collections = TableQuery[CollectionTable]

  object CollectionDAO extends AlbumRepository with CollectionAlbumRepository {

    def findCollectionById(id: Long): Future[Option[Collection]] = {

      val query = collections.filter(_.id === id)
      db.run(query.result.headOption)
    }

    def getFeaturedCollections(page: Int = 1, limit: Int = 10): Future[Page[Collection]] = {
      val offset = limit * (page - 1)
      val query = collections.filter(_.featured).sortBy(_.createdAt.desc.nullsFirst)

      for {
        total <- db.run(query.groupBy(_ => 0).map(_._2.length).result)
        collections <- db.run(query.result)
      } yield Page(collections, page, offset, total.headOption)
    }

    def getCollectionAlbums(collectionID: Long, page: Int = 1, limit: Int = 10): Future[Page[Album]] = {

      val offset = limit * (page - 1)
      val albumJoin = for {
        a <- albums
        ca <- collectionAlbums filter (_.collectionId === collectionID) if a.id === ca.albumId
      } yield a

      for {
        total <- db.run(albumJoin.length.result)
        albums <- db.run(albumJoin.drop(offset).take(limit).result)
      } yield Page(albums, page, offset, Some(total))

    }
  }

}
