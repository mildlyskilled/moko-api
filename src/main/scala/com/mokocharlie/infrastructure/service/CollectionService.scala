package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Collection
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.CollectionRepository

class CollectionService(repo: CollectionRepository)(implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def list(page: Int, limit: Int): ServiceResponse[Page[Collection]] =
    dbExecute(repo.list(page, limit))

  def featuredCollection(page: Int, limit: Int): ServiceResponse[Page[Collection]] =
    dbExecute(repo.getFeaturedCollections(page, limit))

  def collectionById(id: Long): ServiceResponse[Option[Collection]] =
    dbExecute(repo.findCollectionById(id))

}
