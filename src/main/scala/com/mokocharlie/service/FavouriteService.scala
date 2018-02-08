package com.mokocharlie.service

import java.sql.Timestamp
import java.time.{Clock, Instant}

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Favourite
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.FavouriteRepository

class FavouriteService(repo: FavouriteRepository, clock: Clock)(implicit val system: ActorSystem)
    extends MokoCharlieService {

  def imageFavourites(photoId: Long, page: Int, limit: Int): ServiceResponse[Page[Favourite]] =
    dbExecute(repo.favouritesByPhotoId(photoId, page, limit))

  def addFavourite(userId: Long, photoId: Long): ServiceResponse[Long] =
    dbExecute(repo.addFavourite(userId, photoId, Timestamp.from(Instant.now(clock))))
}
