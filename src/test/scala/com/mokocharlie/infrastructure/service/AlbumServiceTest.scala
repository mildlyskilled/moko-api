package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.{AlbumRepository, PhotoRepository}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

class AlbumServiceTest extends FlatSpec with Matchers {
  implicit val system = ActorSystem("test-system")

  behavior of "AlbumService"

  val config = ConfigFactory.load()
  val photoRepository = new PhotoRepository(config)
  val albumRepository = new AlbumRepository(config, photoRepository)
  val albumService = new AlbumService(albumRepository)
  "AlbumService" should " return a page of items " in {

  }

  it should "return a EmptyResultError when given a non-existent id " in {

  }

}
