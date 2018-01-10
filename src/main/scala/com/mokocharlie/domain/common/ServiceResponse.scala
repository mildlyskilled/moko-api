package com.mokocharlie.domain.common

import scala.concurrent.Future

object ServiceResponse {
  type RepositoryResponse[T] = Either[MokoCharlieServiceError, T]
  type ServiceResponse[T] = Future[RepositoryResponse[T]]
}