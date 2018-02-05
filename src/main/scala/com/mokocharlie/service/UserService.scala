package com.mokocharlie.service

import java.sql.Timestamp
import java.time.{Clock, Instant}

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.MokoCharlieServiceError.{AuthenticationError, EmptyResultSet}
import com.mokocharlie.domain.{Page, Token}
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.{TokenRepository, UserRepository}
import com.mokocharlie.infrastructure.spartan.BearerTokenGenerator
import io.github.nremond.SecureHash

class UserService(
    repo: UserRepository,
    tokenRepo: TokenRepository,
    tokenGenerator: BearerTokenGenerator,
    clock: Clock)(implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def list(page: Int, limit: Int): ServiceResponse[Page[User]] =
    dbExecute(repo.list(page, limit))

  def createOrUpdate(user: User): ServiceResponse[Long] =
    dbExecute {
      repo
        .user(user.id)
        .map(_ ⇒ repo.update(user))
        .getOrElse(repo.create(user))
    }

  def userById(userId: Long): ServiceResponse[User] =
    dbExecute(repo.user(userId))

  def userByEmail(email: String): ServiceResponse[User] =
    dbExecute(repo.user(email))

  def changePassword(
      id: Long,
      currentPassword: String,
      newPassword: String): ServiceResponse[Long] =
    dbExecute(repo.changePassword(id, currentPassword, newPassword))

  def auth(email: String, password: String): ServiceResponse[Token] =
    dbExecute {
      repo
        .user(email)
        .filterOrElse(
          u ⇒ SecureHash.validatePassword(password, u.password.value),
          AuthenticationError("Invalid credentials provided"))
        .flatMap { u ⇒
          val toke = tokenGenerator.generateSHAToken("moko")
          val refresh = tokenGenerator.generateSHAToken(toke)
          tokenRepo.store(
            Token(toke, refresh, u.id, Timestamp.from(Instant.now(clock).plusSeconds(15 * 60))))
        }
    }

  def refreshToken(refreshToken: String): ServiceResponse[Token] =
    dbExecute { tokenRepo.refresh(refreshToken, Timestamp.from(Instant.now(clock))) }

  def validateToken(token: String): ServiceResponse[Boolean] =
    dbExecute {
      tokenRepo
        .check(token)
        .map(t ⇒ t.value == token && t.expiresAt.after(Timestamp.from(Instant.now(clock))))
    }

  def userByToken(token: String): ServiceResponse[User] =
    dbExecute {
      tokenRepo
        .check(token)
        .filterOrElse(
          token ⇒ token.expiresAt.after(Timestamp.from(Instant.now(clock))),
          EmptyResultSet(s"Could not get user by token: $token")
        )
        .flatMap { t ⇒
          repo.userByToken(t.value)
        }
    }
}
