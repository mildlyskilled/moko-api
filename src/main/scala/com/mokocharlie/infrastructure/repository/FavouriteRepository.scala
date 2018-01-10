package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Favourite
import com.mokocharlie.domain.Page
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

class FavouriteRepository(override val config: Config)
  extends JdbcRepository
  with StrictLogging {

    def findFavouritesByImageID(imageID: Long, page: Int, limit: Int): Future[Page[Favourite]] = ???

    def findFavouritesByUserAndImage(imageID: Long, userID: Long): Future[Option[Favourite]] = ???

}
