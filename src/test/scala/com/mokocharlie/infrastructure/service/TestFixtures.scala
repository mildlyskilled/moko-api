package com.mokocharlie.infrastructure.service

import java.sql.Timestamp
import java.time.Instant

import scalikejdbc._
import com.mokocharlie.domain.MokoModel.{Album, Photo}

trait TestFixtures {
  val photo1 = Photo(
    id = 1,
    imageId = "legacy_1",
    name = "A test image",
    caption = "This is a test image",
    createdAt = Timestamp.from(Instant.now()),
    updatedAt = Some(Timestamp.from(Instant.now())),
    path = Some("12323453245.jpb"),
    ownerId = 1,
    deletedAt = None,
    published = true,
    cloudImage = None
  )

  val album1 = Album(
    id = 1,
    albumId = Some(12L),
    label = "Test album",
    description = "This is a test album",
    cover = Some(photo1),
    createdAt = Timestamp.from(Instant.now()),
    updatedAt = Some(Timestamp.from(Instant.now())),
    published = true,
    featured = false
  )

  val album2: Album = album1.copy(id = 2, cover = None)

}
