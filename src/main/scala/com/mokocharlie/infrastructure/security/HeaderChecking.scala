package com.mokocharlie.infrastructure.security

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.MokoCharlieServiceError.OperationDisallowed
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.inbound.routing.HttpErrorMapper
import com.mokocharlie.service.UserService

trait HeaderChecking extends HttpErrorMapper{

  val userService: UserService

  val userToken = "X-MOKO-USER-TOKEN"

  def extractUser: Directive1[ServiceResponse[User]] =
    optionalHeaderValueByName(userToken).flatMap{
      case Some(token) ⇒ provide(userService.userByToken(token))
      case None ⇒ completeWithError(OperationDisallowed("You need to be authenticated to look at this data"))
    }

}
