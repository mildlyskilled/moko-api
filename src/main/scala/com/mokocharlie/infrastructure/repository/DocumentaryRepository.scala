package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Documentary
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

class DocumentaryRepository(override val config: Config)
  extends JdbcRepository
  with StrictLogging {

    def list(page: Int, limit: Int): Future[Page[Documentary]] = ???
    def findDocumentaryByID(id: Long): Future[Option[Documentary]] = ???

}
