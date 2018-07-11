package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.db.DBHealthCheck
import com.mokocharlie.service.HealthCheckService
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import akka.testkit.TestDuration

import scala.concurrent.duration._

class HealthCheckRoutingTest extends FlatSpec with ScalatestRouteTest with Matchers with JsonConversion{
  val config: Config = ConfigFactory.load()
  val healthCheckRepo = new DBHealthCheck(config)
  val healthCheckService: HealthCheckService = new HealthCheckService(healthCheckRepo)
  val healthCheckRoute: Route = new HealthCheckRouting(healthCheckService).routes
  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  "Health check" should "return health response" in {
    Get("/healthcheck") ~> healthCheckRoute ~> check {
      responseAs[String] shouldBe "Database Healthy"
    }
  }
}
