package com.mokocharlie.infrastructure.repository.db

import com.mokocharlie.domain.MokoModel.{Album, Contact, Hospitality, Photo}
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
              ${selectPublished(publishedOnly)}
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
          | c.owner_id,
          |	a.id as albumId,
          |	a.album_id,
          |	a.label,
          |	a.description,
          |	a.created_at,
          |	a.updated_at,
          |	a.published,
          |	a.featured,
          |	a.cover_id,
          |	p.id AS photo_id,
          |	p.image_id AS legacy_id,
          |	p.name AS photo_name,
          |	p.caption AS photo_caption,
          |	p.created_at AS photo_created_at,
          |	p.deleted_at AS photo_deleted_at,
          |	p.`owner` AS photo_owner,
          |	p.path AS photo_path,
          |	p.`updated_at` AS photo_updated_at,
          |	p.cloud_image,
          |	p.published AS photo_published,
          | (SELECT COUNT(photo_id) FROM common_photo_albums AS cap WHERE cap.album_id = a.id) AS photo_count,
          | (SELECT COUNT(c.comment_id) FROM common_comment AS c WHERE c.image_id = p.id AND c.comment_approved) AS comment_count,
          | (SELECT COUNT(f.id) FROM common_favourite AS f WHERE f.photo_id = p.id) AS favourite_count
          | FROM common_hospitality AS h
          | LEFT JOIN common_contact AS c ON c.id = h.contact_id
          | LEFT JOIN common_hospitality_albums AS ha ON ha.album_id =
          |   (SELECT MIN(ha2.album_id) FROM common_hospitality_albums AS ha2 WHERE ha2.hospitality_id = h.id)
          | LEFT JOIN common_album AS a ON a.id = ha.album_id
          | LEFT JOIN common_photo p ON a.cover_id = p.id
      """.stripMargin
  }

  override def hospitalityByType(
      hType: HospitalityType,
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): RepositoryResponse[Page[Hospitality]] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          val hospitality =
            sql"$defaultSelect WHERE h.hospitality_type = ${hType.value} ${selectPublished(publishedOnly, "AND")}"
              .map(r ⇒ toHospitality(r))
              .list
              .apply()
          Right(
            Page(
              hospitality,
              page,
              offset(page, limit),
              total(sqls"WHERE h.hospitality_type = ${hType.value}").toOption))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    } catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }

  def featured(page: Int, limit: Int, publishedOnly: Option[Boolean]): RepositoryResponse[Page[Hospitality]] =
    try{
      readOnlyTransaction{ implicit session ⇒
        try{
          val res = sql"""
              $defaultSelect
              WHERE h.featured = 1
              ${selectPublished(publishedOnly, "AND")}
            """
            .map(toHospitality)
            .list
            .apply()
          Right(Page(res, page, offset(page, limit), total().toOption))
        } catch {
          case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
        }
      }
    }catch {
      case ex: Exception ⇒ Left(DatabaseServiceError(ex.getMessage))
    }
  private def total(wherePredicate: SQLSyntax = sqls""): RepositoryResponse[Int] =
    try {
      readOnlyTransaction { implicit session ⇒
        try {
          sql"""SELECT COUNT(id) AS count FROM common_hospitality AS h $wherePredicate"""
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
    val photo = rs.longOpt("cover_id").map { _ ⇒
      Photo(
        rs.int("photo_id"),
        rs.stringOpt("legacy_id"),
        rs.string("photo_name"),
        rs.stringOpt("photo_path"),
        rs.string("photo_caption"),
        rs.timestamp("photo_created_at"),
        rs.timestampOpt("photo_updated_at"),
        rs.int("photo_owner"),
        rs.boolean("photo_published"),
        rs.timestampOpt("photo_deleted_at"),
        rs.stringOpt("cloud_image"),
        rs.int("comment_count"),
        rs.int("favourite_count")
      )
    }

    val album = Album(
      rs.int("albumId"),
      rs.longOpt("album_id"),
      rs.string("label"),
      rs.string("description"),
      photo,
      rs.timestamp("created_at"),
      rs.timestampOpt("updated_at"),
      rs.boolean("published"),
      rs.boolean("featured"),
      rs.int("photo_count")
    )

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
      ),
      album = album
    )
  }

  private def selectPublished(publishedOnly: Option[Boolean], joiner: String = "WHERE") =
    publishedOnly
      .map { p ⇒
        val j = joiner match {
          case "AND" ⇒ sqls"AND"
          case _ ⇒ sqls"WHERE"
        }
        sqls" $j h.published = $p"
      }
      .getOrElse(sqls"")
}
