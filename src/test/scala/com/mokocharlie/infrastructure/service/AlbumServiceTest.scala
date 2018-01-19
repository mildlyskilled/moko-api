package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.{DBAlbumRepository, DBPhotoRepository}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

class AlbumServiceTest extends FlatSpec with Matchers {
  implicit val system = ActorSystem("test-system")

  behavior of "AlbumService"

  val config = ConfigFactory.load()
  println(config.getString("mokocharlie.db.host"))
  val photoRepository = new DBPhotoRepository(config)
  val albumRepository = new DBAlbumRepository(config, photoRepository)
  val albumService = new AlbumService(albumRepository)
  "AlbumService" should " return a page of items " in {}

  it should "return a EmptyResultError when given a non-existent id " in {
    println(albumService.list(1, 10))
  }

}
