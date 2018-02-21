package com.mokocharlie.infrastructure.inbound.routing

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.mokocharlie.infrastructure.outbound.JsonConversion
import com.mokocharlie.infrastructure.repository.db.{DBContactRepository, DBHospitalityRepository}
import com.mokocharlie.service.{ContactService, HospitalityService}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import akka.testkit.TestDuration
import com.mokocharlie.domain.MokoModel.Hospitality
import com.mokocharlie.domain.Page

import scala.concurrent.duration._

class HospitalityRoutingTest extends FlatSpec with ScalatestRouteTest with Matchers with JsonConversion {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)
  val config = ConfigFactory.load()
  val hospitalityRepo = new DBHospitalityRepository(config)
  val contactRepo = new DBContactRepository(config)
  val contactService = new ContactService(contactRepo)
  val hospitalityService = new HospitalityService(hospitalityRepo, contactService)

  val hospitalityRoute = new HospitalityRouting(hospitalityService).routes

  "Hospitality route " should "retrieve all hospitality entries" in {
    Get("/hospitality") ~> hospitalityRoute ~> check {
      val page = responseAs[String].parseJson.convertTo[Page[Hospitality]]
      page.items shouldBe 14
    }
  }
}
