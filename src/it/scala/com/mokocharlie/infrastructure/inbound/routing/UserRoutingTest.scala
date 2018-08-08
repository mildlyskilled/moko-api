package com.mokocharlie.infrastructure.inbound.routing
import java.time.LocalDateTime

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.SettableClock
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.ITFakeTokenRepository
import com.mokocharlie.infrastructure.repository.db.DBUserRepository
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service.UserService
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import akka.testkit.TestDuration

import scala.concurrent.duration._
import spray.json._

class UserRoutingTest extends FlatSpec with ScalatestRouteTest with Matchers with JsonConversion {
  val config: Config = ConfigFactory.load()
  val userRepository = new DBUserRepository(config)
  val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  val userService = new UserService(
    userRepository,
    new ITFakeTokenRepository(config, clock),
    new BearerTokenGenerator(),
    clock)
  val userRoutes: Route = new UserRouting(userService).routes
  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  it should "return a user response given an ID " in {
    val header = RawHeader("X-MOKO-UER-TOKEN", "testtoken")
    Get("/users/1").addHeader(header) ~> userRoutes ~> check {
      val user = responseAs[String].parseJson.convertTo[User]
      user.lastName shouldBe "Aning"
      user.id shouldBe 1
    }
  }
}
