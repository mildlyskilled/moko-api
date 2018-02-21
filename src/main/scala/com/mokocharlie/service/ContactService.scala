package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Contact
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.ContactRepository
import com.typesafe.scalalogging.StrictLogging

class ContactService(contactRepo: ContactRepository)(implicit val system: ActorSystem)
    extends MokoCharlieService
    with StrictLogging {

  def createOrUpdate(contact: Contact): ServiceResponse[Long] =
    dbExecute {
      contactRepo
        .contactById(contact.id)
        .map { _ ⇒
          logger.info(s"Found $contact updating...")
          contactRepo.update(contact)
        }
        .getOrElse {
          logger.info(s"Didn't find $contact creating...")
          contactRepo.create(contact)
        }
    }
}
