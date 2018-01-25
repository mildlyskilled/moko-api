package com.mokocharlie.infrastructure.service

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}

import com.mokocharlie.SettableClock
import com.mokocharlie.domain.MokoModel.{Album, Collection, Photo}

trait TestFixtures {
  val clock: SettableClock = new SettableClock(LocalDateTime.of(2018, 1, 25, 12, 24, 0))
  val photo1 = Photo(
    id = 1,
    imageId = Some("legacy_1"),
    name = "A test image",
    caption = "This is a test image",
    createdAt = Timestamp.from(Instant.now(clock)),
    updatedAt = Some(Timestamp.from(Instant.now(clock))),
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
    createdAt = Timestamp.from(Instant.now(clock)),
    updatedAt = Some(Timestamp.from(Instant.now(clock))),
    published = true,
    featured = false
  )

  val collection1 = Collection(
    id = 1L,
    name = "Hotels",
    featured = true,
    description = "This is a test collection",
    createdAt = Timestamp.from(Instant.now(clock)),
    updatedAt = Timestamp.from(Instant.now(clock)),
    coverAlbumId = 1L
  )

  val album2: Album = album1.copy(id = 2, cover = None)

}
