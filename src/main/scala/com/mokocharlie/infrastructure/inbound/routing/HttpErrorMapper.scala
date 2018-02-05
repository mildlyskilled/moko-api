package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.common.MokoCharlieServiceError._

trait HttpErrorMapper {

  private def toHttpError(serviceError: MokoCharlieServiceError): APIError = serviceError match {
    case EmptyResultSet(x) ⇒ APIError(StatusCodes.NotFound, x)
    case DatabaseServiceError(x) ⇒ APIError(StatusCodes.InternalServerError, x)
    case AuthenticationError(x) ⇒ APIError(StatusCodes.Unauthorized, x)
    case MaximumNumberOfAttemptsReached(msg, ourError, _) ⇒
      ourError
        .map(toHttpError)
        .getOrElse(APIError(StatusCodes.InternalServerError, msg))
    case OperationDisallowed(x) ⇒ APIError(StatusCodes.Forbidden, x)
    case ex ⇒ APIError(StatusCodes.InternalServerError, ex.msg)
  }

  def completeWithError(error: MokoCharlieServiceError): StandardRoute = {
    val httpError = toHttpError(error)
    complete(httpError.code, httpError.msg)
  }
}
