package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.Hospitality
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.HospitalityRepository
import com.typesafe.scalalogging.StrictLogging

class HospitalityService(repo: HospitalityRepository)(implicit val system: ActorSystem)
    extends MokoCharlieService
    with StrictLogging {

  def list(
      page: Int,
      limit: Int,
      publishedOnly: Option[Boolean]): ServiceResponse[Page[Hospitality]] =
    dbExecute(repo.list(page, limit, publishedOnly))

  def createOrUpdate(hospitality: Hospitality): ServiceResponse[Long] =
    dbExecute {
      repo
        .hospitalityById(hospitality.id)
        .map { _ ⇒
          logger.info(s"${hospitality.hospitalityType} with id ${hospitality.id} exists updating")
          repo.update(hospitality)
        }
        .getOrElse(repo.create(hospitality))
    }
}
