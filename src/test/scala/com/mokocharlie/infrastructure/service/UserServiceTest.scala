package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.DBUserRepository
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

import scala.concurrent.ExecutionContext

@DoNotDiscover
class UserServiceTest extends AsyncFlatSpec with TestFixtures with TestDBUtils with Matchers {
  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val ec: ExecutionContext = system.dispatcher
  val config: Config = ConfigFactory.load()
  val userRepo = new DBUserRepository(config)
  val userService = new UserService(userRepo)
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
    userService.createOrUpdate(user1.copy(firstName = "Kobby")).flatMap{
      case Right(id) ⇒ userService.userById(id).map{
        case Right(user) ⇒ user.firstName shouldBe "Kobby"
        case Left(ex) ⇒ fail(s"A user should have been retreived ${ex.msg}")
      }
      case Left(ex) ⇒ fail(s"An update should have occured ${ex.msg}")
    }
  }

  it should "retrieve a list of users" in {
    userService.list(1, 5).map{
      case Right(usersPage) ⇒ usersPage should not be empty
      case Left(ex) ⇒ fail(s"A list of users should be returned ${ex.msg}")
    }
  }

  it should "change a password after verifying an older one"

  it should "reset a password"
}
