package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mokocharlie.service.HealthCheckService

class HealthCheckRouting(healthCheckService: HealthCheckService) extends HttpUtils {

  val routes: Route = {
    path("healthcheck") {
      get {
        onSuccess(healthCheckService.healthCheck) {
          case Right(_) ⇒ complete("Database Healthy")
          case Left(ex) ⇒ completeWithError(ex)
        }
      }
    }
  }
}
