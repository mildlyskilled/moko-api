package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Collection
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.CollectionRepository

import scala.collection.immutable.Seq

class CollectionService(repo: CollectionRepository)(implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def list(page: Int, limit: Int): ServiceResponse[Page[Collection]] =
    dbExecute(repo.list(page, limit, Some(true)))

  def createOrUpdate(collection: Collection): ServiceResponse[Long] =
    dbExecute {
      repo
        .collectionById(collection.id)
        .map(_ ⇒ repo.update(collection))
        .getOrElse(repo.create(collection))
    }

  def featuredCollection(page: Int, limit: Int): ServiceResponse[Page[Collection]] =
    dbExecute(repo.featuredCollections(page, limit))

  def collectionById(id: Long): ServiceResponse[Collection] =
    dbExecute(repo.collectionById(id))

  def saveAlbumToCollection(collectionId: Long, albumIds: Seq[Long]): ServiceResponse[Seq[Int]] =
    dbExecute(repo.saveAlbumToCollection(collectionId, albumIds))

  def removeAlbumFromCollection(collectionId: Long, albumIds: Seq[Long]): ServiceResponse[Seq[Int]] =
    dbExecute(repo.removeAlbumFromCollection(collectionId, albumIds))

}
