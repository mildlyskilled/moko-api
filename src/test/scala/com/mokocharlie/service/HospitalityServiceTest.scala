package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.db._
import org.scalatest.{AsyncFlatSpec, DoNotDiscover, Matchers}

@DoNotDiscover
class HospitalityServiceTest
    extends AsyncFlatSpec
    with Matchers
    with TestFixtures
    with TestDBUtils {

  implicit val system: ActorSystem = ActorSystem("hospitality-system")
  val contactRepo = new DBContactRepository(config)
  val contactService = new ContactService(contactRepo)
  val photoRepository = new DBPhotoRepository(config)
  val commentRepository = new DBCommentRepository(config)
  val photoService = new PhotoService(photoRepository, commentRepository)
  val albumRepository = new DBAlbumRepository(config, photoRepository)
  val albumService = new AlbumService(albumRepository, photoService)
  val repo = new DBHospitalityRepository(config, albumRepository)
  val hospitalityService = new HospitalityService(repo, contactService, albumService)


  behavior of "HospitalityService"

  "HospitalityService" should "create a new resort" in {
    hospitalityService.createOrUpdate(resort1).map{
      case Right(id) ⇒ id shouldBe 1
      case Left(ex) ⇒ fail(s"A resort should have been created ${ex.msg}")
    }
  }

  it should "retrieve specified hospitality item " in {
    hospitalityService.hospitalityById(1).map{
      case Right(h) ⇒ h shouldBe resort1.copy(album = resort1.album.copy(photoCount = 1, cover = Some(photo1.copy(favouriteCount = 1, commentCount = 1))))
      case Left(ex) ⇒ fail(s"Hospitality with id 1 should have been return ${ex.msg}")
    }
  }

  it should "retrieve a list of hospitality items" in {
    hospitalityService.list(1, 20, Some(true)).map{
      case Right(page) ⇒ page.items should have size 1
      case Left(ex) ⇒ fail(s"Hospitality venues should be returned ${ex.msg}")
    }
  }
}
