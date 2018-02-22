package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Hospitality
import com.mokocharlie.domain.common.MokoCharlieServiceError.EmptyResultSet
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.domain.{HospitalityType, Page}
import com.mokocharlie.infrastructure.repository.HospitalityRepository
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

class HospitalityService(repo: HospitalityRepository, contactService: ContactService)(
    implicit val system: ActorSystem)
    extends MokoCharlieService
    with StrictLogging {

  implicit val ec: ExecutionContext = system.dispatcher

  def list(
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): ServiceResponse[Page[Hospitality]] =
    dbExecute(repo.list(page, limit, publishedOnly))

  def createOrUpdate(hospitality: Hospitality): ServiceResponse[Long] =
    contactService
      .createOrUpdate(hospitality.contact)
      .map {
        case Right(contactId) ⇒
          logger.info(s"THIS IS THE NEW contactID $contactId")
          repo
            .hospitalityById(hospitality.id) match {
            case Right(_) ⇒
              repo.update(hospitality.copy(contact = hospitality.contact.copy(id = contactId)))
            case Left(EmptyResultSet(_)) ⇒
              repo.create(hospitality.copy(contact = hospitality.contact.copy(id = contactId)))
            case Left(e) ⇒ Left(e)
          }

        case Left(e) ⇒ Left(e)
      }


  def hospitalityByType(hType: HospitalityType, page: Int, limit: Int, publishedOnly : Option[Boolean]): ServiceResponse[Page[Hospitality]] =
    dbExecute{repo.hospitalityByType(hType, page, limit, publishedOnly)}

  def hospitalityById(id: Long): ServiceResponse[Hospitality] =
    dbExecute(repo.hospitalityById(id))

  def featured(page: Int, limit: Int, publishedOnly: Option[Boolean]): ServiceResponse[Page[Hospitality]] =
    dbExecute(repo.featured(page, limit, publishedOnly))
}
