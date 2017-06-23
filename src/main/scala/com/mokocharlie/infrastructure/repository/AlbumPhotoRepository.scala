package com.mokocharlie.infrastructure.repository

import com.mokocharlie.connection.Database
import com.mokocharlie.domain.PhotoAlbum
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._


trait AlbumPhotoRepository extends Database
  with PhotoRepository
  with AlbumRepository {

  class PhotoAlbumTable(tag: Tag) extends Table[PhotoAlbum](tag, "common_photo_albums") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def photoID = column[Long]("photo_id")

    def albumID = column[Long]("album_id")

    def * = (id, photoID, albumID) <>((PhotoAlbum.apply _).tupled, PhotoAlbum.unapply)

    def photoFK = foreignKey("photo_fk", photoID, photos)(_.id)

    def albumFK = foreignKey("album_fk", albumID, albums)(_.id)
  }

  val photoAlbums = TableQuery[PhotoAlbumTable]
}