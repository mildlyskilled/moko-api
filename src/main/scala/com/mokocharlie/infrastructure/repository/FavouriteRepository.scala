package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel.Favourite
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait FavouriteRepository {
  def favouritesByPhotoId(id: Long, page: Int, limit: Int): RepositoryResponse[Page[Favourite]]

  def addFavourite(userId: Long, photoId: Long, createdAt: Timestamp): RepositoryResponse[Long]
}
