package com.mokocharlie.incoming.connection

import slick.driver.MySQLDriver.api._
import com.mchange.v2.c3p0.ComboPooledDataSource

trait Database {
  val cpds = new ComboPooledDataSource
  val db = Database.forDataSource(cpds, None)
}
