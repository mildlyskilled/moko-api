package com.mokocharlie.domain.common

sealed trait RequestEntity

object RequestEntity {
  final case class AuthRequest(email: String, password: String) extends RequestEntity
}
