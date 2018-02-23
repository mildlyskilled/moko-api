package com.mokocharlie.infrastructure.inbound.routing

import java.time.LocalDateTime

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Story
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.SettableClock
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.db.{DBStoryRepository, DBTokenRepository, DBUserRepository}
import com.mokocharlie.infrastructure.security.BearerTokenGenerator
import com.mokocharlie.service.{StoryService, UserService}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.duration._

class PhotoStoryRoutingTest
    extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with JsonConversion {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  private val config: Config = ConfigFactory.load()
  private val storyRepository = new DBStoryRepository(config)
  private val storyService = new StoryService(storyRepository)
  private val userRepository = new DBUserRepository(config)
  private val tokenRepository = new DBTokenRepository(config)
  private val clock = new SettableClock(LocalDateTime.of(2018, 2, 13, 12, 50, 30))
  implicit val userService: UserService =
    new UserService(userRepository, tokenRepository, new BearerTokenGenerator, clock)
  val storyRoute: Route = new StoryRouting(storyService, userService).routes

  "Story route" should "Return a list of stories" in {
    Get("/stories") ~> storyRoute ~> check {
      val stories = responseAs[String].parseJson.convertTo[Page[Story]]
      stories should have size 6
    }
  }

  it should "Retrieve a story with ID 16" in {
    Get("/stories/16") ~> storyRoute ~> check {
      val story = responseAs[String].parseJson.convertTo[Story]
      story.id shouldBe 16
    }
  }
}
