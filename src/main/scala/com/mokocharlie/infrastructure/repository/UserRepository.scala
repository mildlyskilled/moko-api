package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

class UserRepository(override val config: Config)
  extends JdbcRepository
  with StrictLogging {

  def findUserByID(id: Long): Future[Option[User]] = ???

  def list(limit: Int, page: Int, activeOnly: Boolean = false): Future[Page[User]] = ???
}
