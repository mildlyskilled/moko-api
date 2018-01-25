package com.mokocharlie.infrastructure.service

import scalikejdbc._

trait TestDBUtils {
  def foreignKeys(check: Int) = DB.localTx { implicit session ⇒
    sql"SET FOREIGN_KEY_CHECKS = $check".executeUpdate().apply()
  }

  def purgeUsers(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_mokouser".executeUpdate().apply()
  }

  def purgePhotos(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_photo".executeUpdate().apply()
  }

  def purgePhotoAlbums(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_photo_albums".executeUpdate().apply()
  }

  def purgeAlbums() = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_album".executeUpdate().apply()
  }

  def purgeCollectionAlbums() = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_collection_albums".executeUpdate().apply()
  }

  def purgeCollection() = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_collection".executeUpdate().apply()
  }

  def purgeTables() = {
    foreignKeys(0)
    purgeCollectionAlbums()
    purgeCollection()
    purgePhotoAlbums()
    purgeAlbums()
    purgePhotos()
    foreignKeys(1)
  }
}
