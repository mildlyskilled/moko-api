package com.mokocharlie.infrastructure.repository

import com.mokocharlie.incoming.connection.Database
import com.mokocharlie.domain.CollectionAlbum
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._


trait CollectionAlbumRepository extends Database {

  class CollectionAlbumTable(tag: Tag) extends Table[CollectionAlbum](tag, "common_collection_albums") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def collectionId = column[Long]("collection_id")

    def albumId = column[Long]("album_id")

    def * = (id, collectionId, albumId) <> ((CollectionAlbum.apply _).tupled, CollectionAlbum.unapply)
  }

  val collectionAlbums = TableQuery[CollectionAlbumTable]
}
