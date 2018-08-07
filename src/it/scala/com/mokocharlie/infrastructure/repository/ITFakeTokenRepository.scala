package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp
import java.time.{Clock, Instant}

import com.mokocharlie.domain.Token
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.mokocharlie.service.ITTestFixtures
import com.typesafe.config.Config

class ITFakeTokenRepository(override val config: Config, override val clock: Clock)
    extends TokenRepository
    with JdbcRepository
    with ITTestFixtures {
  val fakeToken =
    Token(
      "somestupidhasgofdatathatmakesnosense",
      "somestupidhasgofdatathatmakesnosense",
      user1.id,
      Timestamp.from(Instant.now(clock)))

  override def check(token: String): RepositoryResponse[Token] = Right(fakeToken)

  override def store(token: Token): RepositoryResponse[Token] = Right(fakeToken)

  override def refresh(refresh: String, threshold: Timestamp): RepositoryResponse[Token] = Right(fakeToken)
}