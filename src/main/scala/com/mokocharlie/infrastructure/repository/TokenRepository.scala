package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp

import com.mokocharlie.domain.Token
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait TokenRepository {
  def check(token: String): RepositoryResponse[Token]

  def store(token: Token): RepositoryResponse[Token]

  def refresh(refresh: String, threshold: Timestamp): RepositoryResponse[Token]

}
