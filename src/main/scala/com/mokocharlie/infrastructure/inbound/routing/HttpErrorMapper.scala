package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.common.MokoCharlieServiceError.{DatabaseServiceError, EmptyResultSet}

case class APIError(code: StatusCode, message: String)
trait HttpErrorMapper {

  private def toHttpError(serviceError: MokoCharlieServiceError): APIError = serviceError match {
    case EmptyResultSet(x) ⇒ APIError(StatusCodes.NotFound, x)
    case DatabaseServiceError(x) ⇒ APIError(StatusCodes.InternalServerError, x)
    case _ ⇒ APIError(StatusCodes.InternalServerError, "An internal service error occurred")
  }

  def completeWithError(error: MokoCharlieServiceError): StandardRoute = {
    val httpError = toHttpError(error)
    complete(httpError.code, httpError.message)
  }
}
