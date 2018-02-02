package com.mokocharlie.infrastructure.repository
import java.sql.Timestamp
import java.time.{Clock, Instant}

import com.mokocharlie.domain.Token
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config

class FakeTokenRepository(override val config: Config, clock: Clock)
    extends TokenRepository
    with JdbcRepository {
  val fakeToken = Token("somestupidhasgofdatathatmakesnosense", Timestamp.from(Instant.now(clock)))

  override def check(token: String): RepositoryResponse[Token] = Right(fakeToken)

  override def store(token: Token): RepositoryResponse[Token] = Right(fakeToken)
}
