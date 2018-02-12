package com.mokocharlie.infrastructure.security

import akka.http.scaladsl.model.HttpHeader
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.inbound.routing.HttpErrorMapper
import com.mokocharlie.service.UserService

trait HeaderChecking extends HttpErrorMapper{

  val userService: UserService

  val userToken = "X-MOKO-USER-TOKEN"

  def extractUserToken: HttpHeader ⇒ Option[ServiceResponse[User]] = {
    case HttpHeader(`userToken`, value) ⇒ Some(userService.userByToken(value))
    case _ ⇒ None
  }
}
