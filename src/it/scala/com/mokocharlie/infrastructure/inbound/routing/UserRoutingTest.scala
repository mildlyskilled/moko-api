package com.mokocharlie.infrastructure.inbound.routing
import java.time.LocalDateTime

import akka.http.javadsl.model.{RequestEntity ⇒ JRequestEntity}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.{RequestEntity, SettableClock}
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.db.{DBTokenRepository, DBUserRepository}
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service.UserService
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import akka.testkit.TestDuration
import com.mokocharlie.domain.Token

import scala.concurrent.duration._
import spray.json._

class UserRoutingTest extends FlatSpec with ScalatestRouteTest with Matchers with JsonConversion {
  val config: Config = ConfigFactory.load()
  val userRepository = new DBUserRepository(config)
  val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  val userService = new UserService(
    userRepository,
    new DBTokenRepository(config, clock),
    new BearerTokenGenerator(),
    clock,
    config.getInt("mokocharlie.auth.token.ttl-in-days"))
  val userRoutes: Route = new UserRouting(userService).routes
  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)
  var token = ""

  "User Route" should "authenticate" in {
    val authRequest = RequestEntity.AuthRequest("contact@kaning.co.uk", "dayl1ght")
    val authEntity = HttpEntity(MediaTypes.`application/json`, authRequest.toJson.toString)
    Post("/auth").withEntity(authEntity) ~> userRoutes ~> check {
      println(responseAs[String])
      val receivedToken = responseAs[String].parseJson.convertTo[Token]
      response.status shouldBe StatusCodes.OK
      token = receivedToken.value
    }
  }

  it should "return a user response given an ID " in {
    val header = RawHeader("X-MOKO-USER-TOKEN", token)
    Get("/users/1").addHeader(header) ~> userRoutes ~> check {
      val user = responseAs[String].parseJson.convertTo[User]
      user.lastName shouldBe "Aning"
      user.id shouldBe 1
    }
  }
}
