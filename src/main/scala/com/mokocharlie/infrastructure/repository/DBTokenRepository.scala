package com.mokocharlie.infrastructure.repository

import java.sql.Timestamp
import java.time.LocalDateTime

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

  def refresh(refresh: String): RepositoryResponse[Token]

}

class DBTokenRepository(override val config: Config)
    extends TokenRepository
    with JdbcRepository
    with StrictLogging {

  def check(token: String): RepositoryResponse[Token] =
    readOnlyTransaction { implicit session ⇒
      try {
        sql"""
          $defaultSelect
          WHERE t.token = $token
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
      try {
        sql"""
            $defaultSelect
            WHERE t.email = ${token.email}
            AND t.expires_at > now()
          """
          .map(toToken)
          .single
          .apply()
          .map { existingToken ⇒
            logger.info(s"Token exists for this user: ${token.email}, returning it")
            Right(existingToken)
          }
          .getOrElse {
            val res =
              sql"""
              INSERT INTO common_token (token, refresh, email, expires_at)
              VALUES(${token.value}, ${token.refreshToken}, ${token.email}, ${token.expiresAt})
              """
                .updateAndReturnGeneratedKey()
                .apply()
            if (res > 0) {
              logger.info("Creating new token")
              Right(token)
            }
            else Left(DatabaseServiceError("Could not store token"))
          }
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  override def refresh(refresh: String): RepositoryResponse[Token] =
    writeTransaction(3, "Could not refresh token") { implicit session ⇒
      try {
        sql"""
            $defaultSelect
            WHERE t.refresh = $refresh
            AND t.expires_at > now()
        """
          .map(toToken)
          .single
          .apply()
          .map { token ⇒
            val newExpiry = Timestamp.valueOf(token.expiresAt.toLocalDateTime.plusMinutes(15))
            val res =
              sql"""
              UPDATE common_token SET
              expires_at = $newExpiry
              WHERE refresh = $refresh
            """.update
                .apply()
            if (res > 0) Right(token.copy(expiresAt = newExpiry))
            else Left(EmptyResultSet("No updates were made"))
          }
          .getOrElse(Left(EmptyResultSet(
            "A valid token was not found or has already expired re-authentication needed")))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  private val defaultSelect: SQLSyntax =
    sqls"""
      SELECT token, refresh, email, expires_at FROM common_token AS t
    """.stripMargin

  private def toToken(rs: WrappedResultSet): Token =
    Token(rs.string("token"), rs.string("refresh"), rs.string("email"), rs.timestamp("expires_at"))

}
