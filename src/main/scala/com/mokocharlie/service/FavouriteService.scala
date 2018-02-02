package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Favourite
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.FavouriteRepository

class FavouriteService(repo: FavouriteRepository)(implicit val system: ActorSystem)
    extends MokoCharlieService {

  def imageFavourites(photoId: Long, page: Int, limit: Int): ServiceResponse[Page[Favourite]] =
    dbExecute(repo.favouritesByPhotoId(photoId, page, limit))

  def addFavourite(favourite: Favourite): ServiceResponse[Long] =
    dbExecute(repo.addFavourite(favourite))
}
