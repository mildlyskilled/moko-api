package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.{Page, Password}
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import io.github.nremond.SecureHash
import scalikejdbc._

trait UserRepository {

  def user(id: Long): RepositoryResponse[User]

  def user(email: String): RepositoryResponse[User]

  def userByToken(token: String): RepositoryResponse[User]

  def update(user: User): RepositoryResponse[Long]

  def create(user: User): RepositoryResponse[Long]

  def list(page: Int, limit: Int): RepositoryResponse[Page[User]]

  def changePassword(
      id: Long,
      currentPassword: String,
      newPassword: String): RepositoryResponse[Long]

}

class DBUserRepository(override val config: Config)
    extends UserRepository
    with RepoUtils
    with JdbcRepository
    with StrictLogging {

  def user(id: Long): RepositoryResponse[User] = readOnlyTransaction { implicit session ⇒
    try {
      sql"""
           $defaultSelect
           WHERE u.id = $id
          """
        .map(toUser)
        .single
        .apply()
        .map(user ⇒ Right(user))
        .getOrElse(Left(EmptyResultSet(s"Could not find user with id: $id")))
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }
  }

  def user(email: String): RepositoryResponse[User] = readOnlyTransaction { implicit session ⇒
    try {
      sql"""
           $defaultSelect
           WHERE u.email = $email
          """
        .map(toUser)
        .single
        .apply()
        .map(user ⇒ Right(user))
        .getOrElse(Left(EmptyResultSet(s"Could not find user with id: $email")))
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }
  }

  def list(page: Int, limit: Int): RepositoryResponse[Page[User]] = readOnlyTransaction {
    implicit session ⇒
      try {
        val users =
          sql"""
          $defaultSelect
          LIMIT ${offset(page, limit)}, $limit
        """.map(toUser)
            .list
            .apply()
        Right(Page(users, page, limit, total().toOption))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
  }

  def changePassword(
      id: Long,
      currentPassword: String,
      newPassword: String): RepositoryResponse[Long] =
    writeTransaction(3, "Could not update password") { implicit session ⇒
      try {
        sql"SELECT password FROM common_mokouser WHERE id = $id"
          .map(rs ⇒ rs.string("password"))
          .single
          .apply()
          .map { pass ⇒
            logger.info(s"${SecureHash.validatePassword(currentPassword, pass)}")
            SecureHash.validatePassword(currentPassword, pass)
          }
          .map { verified ⇒
            if (verified) {
              val res =
                sql"""UPDATE common_mokouser
                  SET password = ${SecureHash.createHash(newPassword)}
                  WHERE id = $id""".update.apply()
              if (res > 0) Right(id)
              else Left(EmptyResultSet("No columns were updated check current password"))
            }
            else {
              Left(EmptyResultSet("Current password mismatch"))
            }
          }
          .getOrElse(Left(EmptyResultSet(s"Did not find a user with id $id")))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def update(user: User): RepositoryResponse[Long] =
    writeTransaction(3, "Could not update user") { implicit session ⇒
      try {
        val res =
          sql"""
          UPDATE common_mokouser SET
          last_login = ${user.lastLogin},
          is_superuser = ${user.isSuperuser},
          email = ${user.email},
          first_name = ${user.firstName},
          last_name = ${user.lastName},
          is_staff = ${user.isStaff},
          is_active = ${user.isActive},
          date_joined = ${user.dateJoined}
          WHERE id = ${user.id}
        """.update.apply()
        if (res > 0) Right(user.id)
        else Left(DatabaseServiceError(s"Could not update user id: ${user.id}"))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def create(user: User): RepositoryResponse[Long] =
    writeTransaction(3, "Could not create user") { implicit session ⇒
      try {
        Right {
          sql"""
            INSERT INTO common_mokouser(
              password,
              last_login,
              is_superuser,
              email,
              first_name,
              last_name,
              is_staff,
              is_active,
              date_joined
            )
            VALUES(
              ${SecureHash.createHash(user.password.value)},
              ${user.lastLogin},
              ${user.isSuperuser},
              ${user.email},
              ${user.firstName},
              ${user.lastName},
              ${user.isStaff},
              ${user.isActive},
              ${user.dateJoined}
            )
         """.updateAndReturnGeneratedKey
            .apply()
        }
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
    }

  def userByToken(token: String): RepositoryResponse[User] =
    readOnlyTransaction { implicit session ⇒
      sql"""
         $defaultSelect
          LEFT JOIN common_token AS t ON t.user_id = u.id
          WHERE t.token = $token
        """
        .map(toUser)
        .single
        .apply()
        .map { u ⇒
          logger.info(s"User acquired by token: $token")
          Right(u)
        }
        .getOrElse {
          val msg = s"Could not find user with given token: $token"
          logger.info(msg)
          Left(EmptyResultSet(msg))
        }
    }

  private def total(): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        sql"SELECT COUNT(id) AS total FROM common_mokouser"
          .map(rs ⇒ Right(rs.int("total")))
          .single
          .apply
          .getOrElse(Left(EmptyResultSet("Could not get users")))
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }
  private val defaultSelect: SQLSyntax =
    sqls"""
      SELECT u.id,
          u.password,
          u.last_login,
          u.is_superuser,
          u.email,
          u.first_name,
          u.last_name,
          u.is_staff,
          u.is_active,
          u.date_joined
      FROM common_mokouser AS u
      """.stripMargin

  private def toUser(rs: WrappedResultSet): User =
    User(
      id = rs.long("id"),
      password = Password(rs.string("password")),
      lastLogin = rs.timestamp("last_login"),
      isSuperuser = rs.boolean("is_superuser"),
      email = rs.string("email"),
      firstName = rs.string("first_name"),
      lastName = rs.string("last_name"),
      isStaff = rs.boolean("is_staff"),
      isActive = rs.boolean("is_active"),
      dateJoined = rs.timestamp("date_joined")
    )
}
