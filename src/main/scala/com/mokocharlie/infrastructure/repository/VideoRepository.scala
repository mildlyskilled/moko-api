package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Video
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future


class VideoRepository(override val config: Config)
  extends JdbcRepository
  with StrictLogging {


  def findVideoByID(id: Long): Future[Option[Video]] = ???

  def list(page: Int, limit: Int): Future[Page[Video]] = ???

}
