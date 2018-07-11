package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait HealthCheckRepository {
  def quickCheck: RepositoryResponse[Boolean]
}
