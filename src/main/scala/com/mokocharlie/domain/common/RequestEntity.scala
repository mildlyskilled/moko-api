package com.mokocharlie.domain.common

import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse

sealed trait RequestEntity

object RequestEntity {
  final case class AuthRequest(email: String, password: String) extends RequestEntity
  final case class FavouriteRequest(userId: Long, photoId: Long) extends RequestEntity
  final case class TokenResponse(token: String, user: ServiceResponse[User])
}
