package com.mokocharlie.infrastructure.repository.db

import com.mokocharlie.domain.MokoModel.{Contact, Hospitality}
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.mokocharlie.domain.{HospitalityType, Page}
import com.mokocharlie.infrastructure.repository.HospitalityRepository
import com.mokocharlie.infrastructure.repository.common.{JdbcRepository, RepoUtils}
import com.typesafe.config.Config
import scalikejdbc._

class DBHospitalityRepository(override val config: Config)
    extends HospitalityRepository
    with JdbcRepository
    with RepoUtils {

  override def list(
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): RepositoryResponse[Page[Hospitality]] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          val hospitality =
            sql"""
                $defaultSelect
             LIMIT ${offset(page, limit)}, $limit
            """
              .map(toHospitality)
              .list
              .apply()
          Right(Page(hospitality, page, offset(page, limit), total().toOption))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  override def hospitalityById(id: Long): RepositoryResponse[Hospitality] =
    try {
      readOnlyTransaction { implicit session ⇒
        sql"""
             $defaultSelect
           WHERE h.id = $id
           """
          .map(h ⇒ Right(toHospitality(h)))
          .single
          .apply
          .getOrElse(Left(EmptyResultSet(s"Could not find hospitality with id: $id")))
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  override def create(hospitality: Hospitality): RepositoryResponse[Long] =
    try {
      writeTransaction(3, s"Could not create new ${hospitality.hospitalityType}") {
        implicit session ⇒
          try {
            val id =
              sql"""
              INSERT INTO common_hospitality (
              featured,
              hospitality_type,
              `name`,
              `description`,
              address,
              website,
              date_added,
              published,
              contact_id
              )
              VALUES(
              ${hospitality.featured},
              ${hospitality.hospitalityType.value},
              ${hospitality.name},
              ${hospitality.description},
              ${hospitality.address},
              ${hospitality.website},
              ${hospitality.dateAdded},
              ${hospitality.published},
              ${hospitality.contact.id}
              )
          """.updateAndReturnGeneratedKey()
                .apply()
            Right(id)
          } catch {
            case ex: Exception ⇒
              Left(DatabaseServiceError(
                s"Could not create ${hospitality.hospitalityType.value} (${hospitality.name}): ${ex.getMessage}"))
          }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  override def update(hospitality: Hospitality): RepositoryResponse[Long] =
    try {
      writeTransaction(3, "Could not run update ") { implicit session ⇒
        try {
          sql"$defaultSelect WHERE h.id = ${hospitality.id}"
            .map(toHospitality)
            .single
            .apply
            .map { h ⇒
              val rows =
                sql"""
                  UPDATE common_hospitality SET
                  featured = ${hospitality.featured},
                  hospitality_type = ${hospitality.hospitalityType.value},
                  name = ${hospitality.name},
                  description = ${hospitality.description},
                  address = ${hospitality.address},
                  date_added = ${hospitality.dateAdded},
                  published = ${hospitality.published},
                  contact_id = ${hospitality.contact.id}
                 WHERE id = ${hospitality.id}
              """.update().apply()
              if (rows > 0) Right(h.id)
              else Left(EmptyResultSet(s"No data was updated for id: ${h.id}"))
            }
            .getOrElse {
              Left(
                EmptyResultSet(
                  s"Could not find ${hospitality.hospitalityType.value} with id ${hospitality.id}"))
            }
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

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

  override def hospitalityByType(
      hType: HospitalityType,
      page: Int,
      limit: Int): RepositoryResponse[Page[Hospitality]] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          val hospitality = sql"$defaultSelect WHERE h.hospitality_type = $hType"
            .map(r ⇒ toHospitality(r))
            .list
            .apply()
          Right(Page(hospitality, page, offset(page, limit), total().toOption))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
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
      hospitalityType = HospitalityType(rs.string("hospitality_type")),
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
