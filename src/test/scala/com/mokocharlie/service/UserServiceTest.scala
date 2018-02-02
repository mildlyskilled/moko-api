package com.mokocharlie.service

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}

import akka.actor.ActorSystem
import com.mokocharlie.SettableClock
import com.mokocharlie.domain.Token
import com.mokocharlie.domain.common.MokoCharlieServiceError.AuthenticationError
import com.mokocharlie.infrastructure.repository.{DBTokenRepository, DBUserRepository, FakeTokenRepository}
import com.mokocharlie.infrastructure.spartan.BearerTokenGenerator
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import io.github.nremond.SecureHash
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContext

@DoNotDiscover
class UserServiceTest
    extends AsyncFlatSpec
    with TestFixtures
    with TestDBUtils
    with Matchers
    with StrictLogging {
  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val ec: ExecutionContext = system.dispatcher
  val config: Config = ConfigFactory.load()
  val userRepo = new DBUserRepository(config)
  val tokenClock: SettableClock =  new SettableClock(LocalDateTime.of(2018, 1, 26, 12, 24, 0))
  val tokenRepo = new FakeTokenRepository(config, tokenClock)
  val userService = new UserService(userRepo, tokenRepo, new BearerTokenGenerator, clock)

  behavior of "UserService"

  "UserService" should "create new user" in {
    userService.createOrUpdate(user1).flatMap {
      case Right(id) ⇒
        userService.userById(id).map {
          case Right(user) ⇒
            user.firstName shouldBe "Kwabena"
            user.lastName shouldBe "Aning"
            user.email shouldBe "kwabena.aning@gmail.com"
          case Left(ex) ⇒ fail(s"A user should have been retrieved ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"A user should have been created ${ex.msg}")
    }
  }

  it should "update an existing user" in {
    userService.createOrUpdate(user1.copy(firstName = "Kobby")).flatMap {
      case Right(id) ⇒
        userService.userById(id).map {
          case Right(user) ⇒ user.firstName shouldBe "Kobby"
          case Left(ex) ⇒ fail(s"A user should have been retrieved ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"An update should have occurred ${ex.msg}")
    }
  }

  it should "retrieve a list of users" in {
    userService.list(1, 5).map {
      case Right(usersPage) ⇒ usersPage should not be empty
      case Left(ex) ⇒ fail(s"A list of users should be returned ${ex.msg}")
    }
  }

  it should s"change a password: ${user1.password.value} to newPassword " in {
    userService.changePassword(user1.id, user1.password.value, "newPassword").flatMap {
      case Right(id) ⇒
        userService.userById(id).map {
          case Right(u) ⇒ SecureHash.validatePassword("newPassword", u.password.value) shouldBe true
          case Left(ex) ⇒ fail(s"Failed to fetch user ${ex.msg}")
        }
      case Left(ex) ⇒ fail(s"The password should be changed ${ex.msg}")
    }
  }

  it should "fail if an invalid password is passed to change password" in {
    userService.changePassword(user1.id, "wrongPassword", "newPassword2").map {
      case Right(_) ⇒ fail("This should fail")
      case Left(ex) ⇒ ex.msg shouldBe "Current password mismatch"
    }
  }

  it should "return a token given the correct username and password" in {
    userService.auth(user1.email, "newPassword").map {
      case Right(authToken) ⇒  authToken shouldBe tokenRepo.fakeToken
      case Left(ex) ⇒ fail(s"An auth token should be returned ${ex.msg}")
    }
  }

  it should "not authenticate given an incorrect username and password" in {
    userService.auth(user1.email, "wrongPassword").map{
      case Right(x) ⇒ fail(s"This should not have succeeded $x")
      case Left(ex) ⇒ ex shouldBe AuthenticationError("Invalid credentials provided")
    }
  }

  it should "validate a token" in {
    userService.validateToken(tokenRepo.fakeToken.value).map{
      case Right(v) ⇒ v shouldBe true
      case Left(ex) ⇒ fail(s"Could not validate toke $ex")
    }
  }
}
