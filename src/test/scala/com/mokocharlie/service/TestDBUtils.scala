package com.mokocharlie.service

import java.util.concurrent.Semaphore

import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

trait TestDBUtils extends StrictLogging {
  val semaphore = new Semaphore(1, true)

  def acquire(): Unit = {
    logger.info("Waiting for database lock")
    semaphore.acquire()
    logger.info("Got database lock")
  }

  def release(): Unit = {
    semaphore.release()
    logger.info("Released database lock")
  }

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

  def purgeComments() = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_comment".executeUpdate().apply()
  }

  def purgeTables() = {
    foreignKeys(0)
    purgeUsers()
    purgeCollectionAlbums()
    purgeCollection()
    purgePhotoAlbums()
    purgeAlbums()
    purgePhotos()
    purgeComments()
    foreignKeys(1)
  }

}
