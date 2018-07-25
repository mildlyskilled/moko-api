package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import com.mokocharlie.service.{HospitalityService, UserService}
import akka.http.scaladsl.server.Directives._
import com.mokocharlie.domain.HospitalityType
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.security.HeaderChecking

import scala.concurrent.ExecutionContext

class HospitalityRouting(
    hospitalityService: HospitalityService,
    override val userService: UserService)(implicit system: ActorSystem)
    extends SprayJsonSupport
    with HttpUtils
    with HeaderChecking {

  implicit val ec: ExecutionContext = system.dispatcher

  import JsonConversion._

  val routes: Route = {
    path("hospitality" / "featured" ~ Slash.?) {
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) ⇒
          onSuccess(hospitalityService.featured(page, limit, None)) {
            case Right(hPage) ⇒ complete(hPage)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
    } ~ path("hospitality" / Segment.? ~ Slash.?) { hospitalityType ⇒
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
            onSuccess(hospitalityService.hospitalityByType(HospitalityType.apply(hospitalityType), pageNumber, limit, None)) {
              case Right(page) ⇒ complete(page)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }
    }
}
