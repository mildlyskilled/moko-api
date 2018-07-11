package com.mokocharlie.infrastructure.repository.db

import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.HealthCheckRepository
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import scalikejdbc._

class DBHealthCheck(override val config: Config) extends HealthCheckRepository with JdbcRepository {
  def quickCheck: RepositoryResponse[Boolean] =
    readOnlyTransaction { implicit session ⇒
      try {
        sql"SELECT COUNT(id) AS total FROM common_photo WHERE published = 1"
          .map(rs ⇒ Right(rs.int("total") > 0))
          .single
          .apply
          .getOrElse(Left(EmptyResultSet("Could not get photos")))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }
}
