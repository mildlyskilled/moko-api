package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Hospitality
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait HospitalityRepository {
  def list(
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): RepositoryResponse[Page[Hospitality]]

  def hospitalityById(id: Long): RepositoryResponse[Hospitality]

  def update(hospitality: Hospitality): RepositoryResponse[Long]

  def create(hospitality: Hospitality): RepositoryResponse[Long]
}
