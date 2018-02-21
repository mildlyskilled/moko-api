package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.db.{DBContactRepository, DBHospitalityRepository}
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

@DoNotDiscover
class HospitalityServiceTest
    extends AsyncFlatSpec
    with Matchers
    with TestFixtures
    with TestDBUtils {

  implicit val system: ActorSystem = ActorSystem("hospital-system")
  val repo = new DBHospitalityRepository(config)
  val contactRepo = new DBContactRepository(config)
  val contactService = new ContactService(contactRepo)
  val hospitalityService = new HospitalityService(repo, contactService)


  behavior of "HospitalityService"

  "HospitalityService" should "create a new resort" in {
    hospitalityService.createOrUpdate(resort1).map{
      case Right(id) ⇒ id shouldBe 1
      case Left(ex) ⇒ fail(s"A resort should have been created ${ex.msg}")
    }
  }


  it should "retrieve a list of hospitality items" in {
    hospitalityService.list(1, 20, Some(true)).map{
      case Right(page) ⇒ page.items should have size 1
      case Left(ex) ⇒ fail(s"Hospitality venues should be returned ${ex.msg}")
    }
  }
}
