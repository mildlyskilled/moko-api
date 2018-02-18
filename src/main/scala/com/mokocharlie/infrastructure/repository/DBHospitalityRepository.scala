package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.{Contact, Hospitality}
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.infrastructure.repository.common.JdbcRepository
import com.typesafe.config.Config
import scalikejdbc._

trait HospitalityRepository {}

class DBHospitalityRepository(override val config: Config)
    extends HospitalityRepository
    with JdbcRepository {

  private val defaultSelect = {
    sqls"""
       | SELECT
       | h.id,
       | h.featured,
       | h.hospitality_type,
       | h.name,
       | h.description,
       | h.address,
       | h.website,
       | h.date_added,
       | h.published,
       | h.contact_id,
       | c.first_name,
       | c.last_name,
       | c.email,
       | c.telephone,
       | c.owner_id
       | FROM common_hospitality AS h
       | LEFT JOIN common_contact AS c ON c.id = h.contact_id
      """.stripMargin
  }

  private def total(): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          sql"""SELECT COUNT(id) AS count FROM common_hospitality"""
            .map(rs ⇒ Right(rs.int("count")))
            .single
            .apply()
            .getOrElse(Left(EmptyResultSet("Could not find any hospitality entries")))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  private def toHospitality(rs: WrappedResultSet): Hospitality = {
    Hospitality(
      id = rs.long("id"),
      name = rs.string("name"),
      featured = rs.boolean("featured"),
      hospitalityType = rs.string("hospitatlity_type"),
      address = rs.string("address"),
      description = rs.string("description"),
      website = rs.string("website"),
      dateAdded = rs.timestamp("date_added"),
      published = rs.boolean("published"),
      contact = Contact(
        id = rs.long("contact_id"),
        firstName = rs.string("first_name"),
        lastName = rs.string("last_name"),
        email = rs.string("email"),
        telephone = rs.string("telephone"),
        owner = rs.long("owner_id")
      )
    )
  }
}
