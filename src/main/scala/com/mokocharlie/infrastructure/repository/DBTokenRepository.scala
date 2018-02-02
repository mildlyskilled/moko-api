package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.Token
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

trait TokenRepository {
  def check(token: String): RepositoryResponse[Token]

  def store(token: Token): RepositoryResponse[Token]

}

class DBTokenRepository(override val config: Config)
    extends TokenRepository
    with JdbcRepository
    with StrictLogging {

  def check(token: String): RepositoryResponse[Token] =
    readOnlyTransaction{ implicit  session ⇒
      try{
        sql"""
          $defaultSelect
          WHERE token = $token
        """
          .map(toToken)
          .single
          .apply()
          .map(t ⇒ Right(t))
          .getOrElse(Left(EmptyResultSet("Token not found")))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def store(token: Token): RepositoryResponse[Token] =
    writeTransaction(3, "Could not store token") { implicit session ⇒
      try{
        val res = sql"""
          INSERT INTO common_token (token, expires_at)
          VALUES(${token.value}, ${token.expiresAt})
        """
          .updateAndReturnGeneratedKey()
          .apply()
        if (res > 0) Right(token)
        else Left(DatabaseServiceError("Could not store token"))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }


  private val defaultSelect: SQLSyntax =
    sqls"""
      SELECT token, expires_at FROM common_token
    """.stripMargin

  private def toToken(rs: WrappedResultSet): Token =
    Token(rs.string("token"), rs.timestamp("expires_at"))
}
