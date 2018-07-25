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
          optionalHeaderValue(extractUserToken) { user ⇒
            user
              .map { userResponse ⇒
                val res = for {
                  u ← userResponse
                  h ← hospitalityService.featured(page, limit, userService.publishedFlag(u))
                } yield h

                onSuccess(res) {
                  case Right(hPage) ⇒ complete(hPage)
                  case Left(error) ⇒ completeWithError(error)
                }
              }.getOrElse {
              onSuccess(hospitalityService.featured(page, limit, Some(true))) {
                case Right(hPage) ⇒ complete(hPage)
                case Left(error) ⇒ completeWithError(error)
              }
            }
          }
        }
      }
    } ~ path("hospitality" / Segment.? ~ Slash.?) { hospitalityType ⇒
      get {
        parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (pageNumber, limit) ⇒
          optionalHeaderValue(extractUserToken) { user ⇒
            user
              .map { userResponse ⇒
                val res = for {
                  u ← userResponse
                  hospitality ← hospitalityType
                    .map { h ⇒
                      hospitalityService
                        .hospitalityByType(
                          HospitalityType.apply(h),
                          pageNumber,
                          limit,
                          userService.publishedFlag(u))
                    }
                    .getOrElse(
                      hospitalityService.list(pageNumber, limit, userService.publishedFlag(u)))
                } yield hospitality

                onSuccess(res) {
                  case Right(page) ⇒ complete(page)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
              .getOrElse {
                val res = hospitalityType
                  .map { h ⇒
                    hospitalityService
                      .hospitalityByType(HospitalityType.apply(h), pageNumber, limit, Some(true))
                  }
                  .getOrElse(hospitalityService.list(pageNumber, limit, Some(true)))
                onSuccess(res) {
                  case Right(page) ⇒ complete(page)
                  case Left(error) ⇒ completeWithError(error)
                }
              }
          }
        }
      }
    }
  }
}
