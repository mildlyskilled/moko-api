package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.HealthCheckRepository

class HealthCheckService(healthCheckRepo: HealthCheckRepository)(implicit val system: ActorSystem)
    extends MokoCharlieService {

  def healthCheck: ServiceResponse[Boolean] =
    dbExecute(healthCheckRepo.quickCheck)

}
