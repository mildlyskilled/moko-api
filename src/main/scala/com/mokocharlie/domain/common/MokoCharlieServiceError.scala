package com.mokocharlie.domain.common

sealed trait MokoCharlieServiceError {
  def msg: String
}

object MokoCharlieServiceError{
  final case class EmptyResultSet(msg: String) extends MokoCharlieServiceError
  final case class DatabaseServiceError(msg: String) extends MokoCharlieServiceError
  final case class UnknownError(exception: Exception) extends MokoCharlieServiceError {
    val msg: String = exception.getMessage
  }
  final case class MaximumNumberOfAttemptsReached(
    msg: String,
    error: Option[MokoCharlieServiceError] = None,
    exception: Option[Exception] = None) extends MokoCharlieServiceError
  final case class AuthenticationError(msg: String) extends MokoCharlieServiceError
}
