package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.mokocharlie.domain.{Mail, MailRecipient}
import com.mokocharlie.domain.common.MokoCharlieServiceError.{APIError, OperationDisallowed, UnknownError}
import com.mokocharlie.domain.common.RequestEntity.{AuthRequest, PasswordResetRequest}
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.{HeaderChecking, RandomStringUtil}
import com.mokocharlie.service.{MailService, UserService}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.Source
import scala.util.{Failure, Random, Success}

class UserRouting(override val userService: UserService, mailService: MailService)(
    implicit system: ActorSystem)
    extends SprayJsonSupport
    with HttpUtils
    with StrictLogging
    with HeaderChecking {

  import JsonConversion._
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val routes: Route = {

    path("users" / LongNumber) { id =>
      get {
        optionalHeaderValue(extractUserToken) { tokenResponse ⇒
          tokenResponse
            .map { userFuture ⇒
              val res = for {
                u ← userFuture.user
                f ← if (u.exists(_.isSuperuser) || u.exists(_.id == id)) userService.userById(id)
                else
                  Future.successful(Left(OperationDisallowed(
                    "You need to be a super user or own this account to access this data")))
              } yield f

              onSuccess(res) {
                case Right(foundUser) ⇒ complete(foundUser)
                case Left(error) ⇒ completeWithError(error)
              }
            }
            .getOrElse(
              completeWithError(OperationDisallowed("Could not retrieve user without a token"))
            )
        }
      }
    } ~
      path("auth" ~ Slash.?) {
        post {
          entity(as[AuthRequest]) { authRequest ⇒
            logger.info(s"Got request from ${authRequest.email}")
            onSuccess(userService.auth(authRequest.email, authRequest.password)) {
              case Right(token) ⇒ complete(token)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      } ~
      path("refresh" / Segment) { refreshToken ⇒
        get {
          onSuccess(userService.refreshToken(refreshToken)) {
            case Right(token) ⇒ complete(token)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      } ~ path("reset-password") {
      post {
        entity(as[PasswordResetRequest]) { passwordResetRequest: PasswordResetRequest ⇒
          val newPassword = RandomStringUtil.randomStringRecursive(10).mkString

          val content = Source
            .fromResource("mail/password-reset.html")
            .getLines
            .mkString
            .replace("{{tempPassword}}", newPassword)

          val to = MailRecipient("Mokocharlie User", passwordResetRequest.email)
          val from = MailRecipient("Mokocharlie Postman", "team@mokocharlie.com")

          val mail = Mail(content, "Reset your password", to, from)
          mailService.send(mail) match {
            case Right(e) ⇒ complete(StatusCodes.Accepted, s"$e")
            case Left(ex) ⇒ completeWithError(ex)
          }
        }
      }
    }
  }
}
