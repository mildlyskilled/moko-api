package com.mokocharlie.infrastructure.service

import org.scalatest.{BeforeAndAfterAll, Suites}

class ServiceTests
    extends Suites(new PhotoServiceTest, new AlbumServiceTest)
    with BeforeAndAfterAll
    with TestDBUtils {
  override def beforeAll(): Unit = {
    acquire()
    purgeTables()
  }

  override def afterAll(): Unit = {
    purgeTables()
    release()
  }
}
