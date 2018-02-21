package com.mokocharlie.infrastructure.repository.db

import com.mokocharlie.domain.MokoModel.Contact
import com.mokocharlie.domain.common.MokoCharlieServiceError.DatabaseServiceError
import com.mokocharlie.domain.{MokoModel, Page}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.ContactRepository
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc._

class DBContactRepository(override val config: Config)
    extends ContactRepository
    with JdbcRepository
    with StrictLogging {

  override def list(page: Int, limit: Int): RepositoryResponse[Page[MokoModel.Contact]] =
    try {
      readOnlyTransaction { implicit session ⇒
        val contacts = sql"""
              $defaultSelect
           """
          .map(toContact)
          .list()
          .apply()

        Right(Page(contacts, page, limit, total().toOption))
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  override def create(contact: MokoModel.Contact): RepositoryResponse[Long] =
    try {
      writeTransaction(3, s"Could not create contact $contact") { implicit session ⇒
        try {
          val id = sql"""
            INSERT INTO common_contact(
              first_name,
              last_name,
              email,
              telephone,
              owner_id) VALUES (
            ${contact.firstName},
            ${contact.lastName},
            ${contact.email},
            ${contact.telephone},
            ${contact.owner}
          """
            .updateAndReturnGeneratedKey()
            .apply()
          Right(id)
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }

      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  override def update(contact: MokoModel.Contact): RepositoryResponse[Long] =
    try{
      writeTransaction(3, s"Could not update $contact") { implicit session ⇒
        try{
          sql"""

            """
            .map(toContact)
            .single
            .apply
            .map{c ⇒
              sql"""
                    """
            }
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(s"Could not update $contact"))
        }
      }
    }catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  private val defaultSelect: SQLSyntax =
    sqls"""SELECT
          id,
          first_name,
          last_name,
          email,
          telephone,
          owner_id
          FROM common_contact
        """

  private def total(): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          sql"SELECT COUNT(id) AS total FROM common_contact"
            .map(rs ⇒ Right(rs.int("total")))
            .single
            .apply
            .getOrElse(Left(DatabaseServiceError("could not get total")))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  private def toContact(rs: WrappedResultSet): Contact =
    Contact(
      id = rs.long("id"),
      firstName = rs.string("firs_name"),
      lastName = rs.string("last_name"),
      email = rs.string("email"),
      telephone = rs.string("telephone"),
      owner = rs.long("ownner_id")
    )
}
