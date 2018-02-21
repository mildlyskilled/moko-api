package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import com.mokocharlie.service.HospitalityService
import akka.http.scaladsl.server.Directives._
import com.mokocharlie.domain.HospitalityType
import com.mokocharlie.infrastructure.outbound.JsonConversion

class HospitalityRouting(hospitalityService: HospitalityService) extends SprayJsonSupport with HttpUtils {

  import JsonConversion._

  val routes: Route = {
    path("/hospitality" / Segment.? ~ Slash.?) { hospitalityType ⇒
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          val published = None
          val res = hospitalityType
            .map(h ⇒ hospitalityService.hospitalityByType(HospitalityType.apply(h), pageNumber, limit, published))
            .getOrElse(hospitalityService.list(pageNumber, limit, published))

          onSuccess(res) {
            case Right(page) ⇒ complete(page)
            case Left(error) ⇒ completeWithError(error)
          }
        }
      }
    }
  }
}
