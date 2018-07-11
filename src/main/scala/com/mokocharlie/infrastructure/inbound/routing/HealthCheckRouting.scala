package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.domain.HealthCheck
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.service.HealthCheckService

class HealthCheckRouting(healthCheckService: HealthCheckService)
  extends SprayJsonSupport with HttpUtils {

  import JsonConversion._
  val routes: Route = {
    path("healthcheck") {
      get {
        onSuccess(healthCheckService.healthCheck) {
          case Right(d) ⇒
            val status = if (d) "OK" else "FAIL"
            complete(HealthCheck(Map("database" → status)))
          case Left(ex) ⇒ completeWithError(ex)
        }
      }
    }
  }
}
