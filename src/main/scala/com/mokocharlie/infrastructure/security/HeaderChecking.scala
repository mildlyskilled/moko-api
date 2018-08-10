package com.mokocharlie.infrastructure.security

import akka.http.scaladsl.model.HttpHeader
import com.mokocharlie.domain.common.RequestEntity.TokenResponse
import com.mokocharlie.infrastructure.inbound.routing.HttpUtils
import com.mokocharlie.service.UserService
import com.typesafe.scalalogging.StrictLogging

trait HeaderChecking extends HttpUtils with StrictLogging {

  val userService: UserService

  val userToken = "X-MOKO-USER-TOKEN"

  def extractUserToken: HttpHeader ⇒ Option[TokenResponse] = {
    case header if header.name.toUpperCase == userToken ⇒
      logger.info(s"Received token header  ${header.name} ${header.value}")
      Some(TokenResponse(header.value, userService.userByToken(header.value)))
    case wrongHeader ⇒
      logger.info(s"Ignoring ${wrongHeader.name} ${wrongHeader.value}")
      None
  }
}
