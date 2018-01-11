package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Collection
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.CollectionRepository

class CollectionService(repo: CollectionRepository)(implicit override val system: ActorSystem) extends MokoCharlieService {

  def featuredCollection(page: Int, limit: Int): ServiceResponse[Page[Collection]] =
    dbExecute(repo.getFeaturedCollections(page, limit))

}
