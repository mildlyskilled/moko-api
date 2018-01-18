package com.mokocharlie.domain.common

trait MokoCharlieServiceError {
  def msg: String
}

object MokoCharlieServiceError{
  case class EmptyResultSet(msg: String) extends MokoCharlieServiceError
  case class DatabaseServiceError(msg: String) extends MokoCharlieServiceError
  case class UnknownError(exception: Exception) extends MokoCharlieServiceError {
    val msg: String = exception.getMessage
  }
  case class MaximumNumberOfAttemptsReached(
    msg: String,
    error: Option[MokoCharlieServiceError] = None,
    exception: Option[Exception] = None) extends MokoCharlieServiceError
}
