package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Collection
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.CollectionRepository

class CollectionService(repo: CollectionRepository)(implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def list(page: Int, limit: Int): ServiceResponse[Page[Collection]] =
    dbExecute(repo.list(page, limit, Some(true)))

  def createOrUpdate(collection: Collection): ServiceResponse[Long] =
    dbExecute {
      repo
        .collectionById(collection.id)
        .map(col ⇒ repo.update(col))
        .getOrElse(repo.create(collection))
    }

  def featuredCollection(page: Int, limit: Int): ServiceResponse[Page[Collection]] =
    dbExecute(repo.featuredCollections(page, limit))

  def collectionById(id: Long): ServiceResponse[Collection] =
    dbExecute(repo.collectionById(id))

}
