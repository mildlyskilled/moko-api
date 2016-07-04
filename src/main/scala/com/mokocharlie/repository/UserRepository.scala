package com.mokocharlie.repository

import java.sql.Timestamp

import com.mokocharlie.model.{User, Page}
import com.mokocharlie.connection.Database
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepository extends Database {


  class UserTable(tag: Tag) extends Table[User](tag, "common_mokouser") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def password = column[String]("password")

    def lastLogin = column[Timestamp]("last_login")

    def isSuperUser = column[Boolean]("is_superuser")

    def email = column[String]("email")

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def isStaff = column[Boolean]("is_staff")

    def isActive = column[Boolean]("is_active")

    def dateJoined = column[Timestamp]("date_joined")

    def * = (
      id,
      password,
      lastLogin,
      isSuperUser,
      email,
      firstName,
      lastName,
      isStaff,
      isActive,
      dateJoined
      ) <>((User.apply _).tupled, User.unapply)
  }

  lazy val users = TableQuery[UserTable]

  object UserDAO {

    def findUserByID(id: Long): Future[Option[User]] = {
      val userQuery = users.filter((user) => user.id === id).result.headOption
      db.run(userQuery)
    }

    def list(limit: Int, page: Int, activeOnly: Boolean = false): Future[Page[User]] = {
      val offset = limit * page

      val query = if (activeOnly) {
        users.filter(_.isActive)
      } else {
        users
      }

      val totalQuery = query.groupBy(_ => 0).map(_._2.length)
      val userQuery = query.take(limit)

      for {
        total <- db.run(totalQuery.result)
        user <- db.run(userQuery.result)
      } yield Page(user, page, offset, total.head)
    }

  }

}
