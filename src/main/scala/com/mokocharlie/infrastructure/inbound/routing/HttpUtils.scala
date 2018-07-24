package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.common.MokoCharlieServiceError._
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.typesafe.scalalogging.StrictLogging
import spray.json._

trait HttpUtils extends JsonConversion with StrictLogging {

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

  def completeWithError(error: MokoCharlieServiceError): StandardRoute ={
    val httpError = toHttpError(error)
    logger.error(httpError.toJson.toString)
    complete(HttpResponse(httpError.code, entity = httpError.toJson.toString))
  }

  def translateSuperUserFlag(isSuperUser: Option[User]): Option[User] =
    isSuperUser.filterNot(_.isSuperuser)
}
