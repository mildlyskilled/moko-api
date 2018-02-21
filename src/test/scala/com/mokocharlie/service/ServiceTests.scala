package com.mokocharlie.service

import org.scalatest.{BeforeAndAfterAll, Suites}

class ServiceTests
    extends Suites(
      new UserServiceTest,
      new PhotoServiceTest,
      new AlbumServiceTest,
      new CollectionServiceTest,
      new CommentServiceTest,
      new FavouriteServiceTest,
      new HospitalityServiceTest
    )
    with BeforeAndAfterAll
    with TestDBUtils {
  override def beforeAll(): Unit = {
    logger.info(s"""Running test on
     | ${config.getString("mokocharlie.db.host")} with ${config.getString("mokocharlie.db.dbName")}
     | user: ${config.getString("mokocharlie.db.user")} and
     | password: ${config.getString("mokocharlie.db.password")}""".stripMargin)
    acquire()
    purgeTables()
  }

  override def afterAll(): Unit = {
    purgeTables()
    release()
  }
}
