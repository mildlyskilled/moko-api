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

  def userById(id: Long): RepositoryResponse[User]

  def update(user: User): RepositoryResponse[Long]

  def create(user: User): RepositoryResponse[Long]

  def list(page: Int, limit: Int): RepositoryResponse[Page[User]]
}

class DBUserRepository(override val config: Config)
    extends UserRepository
    with RepoUtils
    with JdbcRepository
    with StrictLogging {

  def userById(id: Long): RepositoryResponse[User] = readOnlyTransaction { implicit session ⇒
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

  def list(page: Int, limit: Int): RepositoryResponse[Page[User]] = readOnlyTransaction {
    implicit session ⇒
      try {
        val users =
          sql"""
          $defaultSelect
          LIMIT ${dbPage(page)}, ${rowCount(page, limit)}
        """.map(toUser)
            .list
            .apply()
        Right(Page(users, page, limit, total()))
      } catch {
        case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
      }
  }

  def changePassword(password: String): RepositoryResponse[Boolean] = ???

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

  private def total(): Option[Int] =
    readOnlyTransaction { implicit session ⇒
      sql"SELECT COUNT(id) AS total FROM common_mokouser"
        .map(rs ⇒ rs.int("total"))
        .single
        .apply()
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
