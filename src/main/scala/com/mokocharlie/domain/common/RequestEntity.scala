package com.mokocharlie.domain.common

sealed trait RequestEntity

object RequestEntity {
  final case class AuthRequest(email: String, password: String) extends RequestEntity
  final case class FavouriteRequest(userId: Long, photoId: Long) extends RequestEntity
}
