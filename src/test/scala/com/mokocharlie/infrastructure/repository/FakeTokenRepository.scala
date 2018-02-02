package com.mokocharlie.infrastructure.repository
import java.sql.Timestamp
import java.time.Instant

import com.mokocharlie.SettableClock
import com.mokocharlie.domain.Token
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.mokocharlie.service.TestFixtures
import com.typesafe.config.Config

class FakeTokenRepository(override val config: Config, override val clock: SettableClock)
    extends TokenRepository
    with JdbcRepository
    with TestFixtures {
  val fakeToken =
    Token(
      "somestupidhasgofdatathatmakesnosense",
      "somestupidhasgofdatathatmakesnosense",
      user1.email,
      Timestamp.from(Instant.now(clock)))

  override def check(token: String): RepositoryResponse[Token] = Right(fakeToken)

  override def store(token: Token): RepositoryResponse[Token] = Right(fakeToken)

  override def refresh(refresh: String, threshold: Timestamp): RepositoryResponse[Token] = Right(fakeToken)
}
