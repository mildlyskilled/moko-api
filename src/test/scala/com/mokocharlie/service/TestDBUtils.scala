package com.mokocharlie.service

import java.util.concurrent.Semaphore

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

trait TestDBUtils extends StrictLogging {
  val semaphore = new Semaphore(1, true)

  val config: Config =  ConfigFactory.load()
  config.withValue("dbName", ConfigValueFactory.fromAnyRef("mokocharlietest"))

  def acquire(): Unit = {
    logger.info("Waiting for database lock")
    semaphore.acquire()
    logger.info("Got database lock")
  }

  def release(): Unit = {
    semaphore.release()
    logger.info("Released database lock")
  }

  def foreignKeys(check: Int): Int = DB.localTx { implicit session ⇒
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

  def purgeAlbums(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_album".executeUpdate().apply()
  }

  def purgeCollectionAlbums(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_collection_albums".executeUpdate().apply()
  }

  def purgeCollection(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_collection".executeUpdate().apply()
  }

  def purgeComments(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_comment".executeUpdate().apply()
  }

  def purgeTokens(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_token".executeUpdate().apply()
  }

  def purgeFavourites(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_favourite".executeUpdate().apply()
  }

  def purgeTables(): Int = {
    foreignKeys(0)
    purgeUsers()
    purgeCollectionAlbums()
    purgeCollection()
    purgePhotoAlbums()
    purgeAlbums()
    purgePhotos()
    purgeComments()
    purgeTokens()
    purgeFavourites()
    foreignKeys(1)
  }

}
