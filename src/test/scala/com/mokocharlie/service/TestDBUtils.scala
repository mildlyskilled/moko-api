package com.mokocharlie.service

import java.util.concurrent.Semaphore

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

trait TestDBUtils extends StrictLogging {
  val semaphore = new Semaphore(1, true)

  val config: Config = ConfigFactory.load()
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

  private def foreignKeys(check: Int): Int = DB.localTx { implicit session ⇒
    sql"SET FOREIGN_KEY_CHECKS = $check".executeUpdate().apply()
  }

  private def purgeUsers(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_mokouser".executeUpdate().apply()
  }

  private def purgePhotos(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_photo".executeUpdate().apply()
  }

  private def purgePhotoAlbums(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_photo_albums".executeUpdate().apply()
  }

  private def purgeAlbums(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_album".executeUpdate().apply()
  }

  private def purgeCollectionAlbums(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_collection_albums".executeUpdate().apply()
  }

  private def purgeCollection(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_collection".executeUpdate().apply()
  }

  private def purgeComments(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_comment".executeUpdate().apply()
  }

  private def purgeTokens(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_token".executeUpdate().apply()
  }

  private def purgeFavourites(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_favourite".executeUpdate().apply()
  }

  private def purgeHospitality(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_hospitality".executeUpdate().apply()
  }

  private def purgeContacts(): Int = DB.localTx { implicit session ⇒
    sql"TRUNCATE TABLE common_contact".executeUpdate().apply()
  }

  private def purgeStories(): Int = DB.localTx{ implicit session ⇒
    sql"TRUNCATE TABLE common_photostory".executeUpdate().apply()
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
    purgeHospitality()
    purgeContacts()
    purgeStories()
    foreignKeys(1)
  }

}
