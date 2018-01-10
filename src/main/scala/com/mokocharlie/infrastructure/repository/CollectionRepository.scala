package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.Page
import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config

import scala.concurrent.Future

/**
  * Created by kwabena on 01/08/2016.
  */
class CollectionRepository(override val config: Config) extends JdbcRepository {

    def findCollectionById(id: Long): Future[Option[Collection]] = ???

    def getFeaturedCollections(page: Int = 1, limit: Int = 10): Future[Page[Collection]] = ???

    def getCollectionAlbums(collectionID: Long, page: Int = 1, limit: Int = 10): Future[Page[Album]] = ???

}
