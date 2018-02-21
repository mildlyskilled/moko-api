package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Video
import com.mokocharlie.domain.Page

import scala.concurrent.Future

trait VideoRepository {

  def findVideoByID(id: Long): Future[Option[Video]] = ???

  def list(page: Int, limit: Int): Future[Page[Video]] = ???

}
