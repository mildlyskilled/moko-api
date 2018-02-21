package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.Contact
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait ContactRepository {
  def list(page: Int, limit: Int): RepositoryResponse[Page[Contact]]

  def create(contact: Contact): RepositoryResponse[Long]

  def update(contact: Contact): RepositoryResponse[Long]

  def contactById(id: Long): RepositoryResponse[Contact]
}


