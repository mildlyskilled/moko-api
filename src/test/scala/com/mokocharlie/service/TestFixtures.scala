package com.mokocharlie.service

import java.sql.Timestamp
import java.time.{Clock, Instant, LocalDateTime}

import com.mokocharlie.domain.HospitalityType.Resort
import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.Password
import com.mokocharlie.domain.common.SettableClock

trait TestFixtures {
  val clock: Clock = new SettableClock(LocalDateTime.of(2018, 1, 25, 12, 24, 0))
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
    cloudImage = None,
    commentCount = 0,
    favouriteCount = 0
  )

  val photo2 = photo1.copy(id = 2, imageId = Some("legacy_2"), name = "Photo number two")

  val album1 = Album(
    id = 1,
    albumId = Some(12L),
    label = "Test album",
    description = "This is a test album",
    cover = Some(photo1),
    createdAt = Timestamp.from(Instant.now(clock)),
    updatedAt = Some(Timestamp.from(Instant.now(clock))),
    published = true,
    featured = false,
    photoCount = 0
  )
  val album2: Album = album1.copy(id = 2, cover = None)

  val collection1 = Collection(
    id = 1L,
    name = "Hotels",
    featured = true,
    published = true,
    description = "This is a test collection",
    createdAt = Timestamp.from(Instant.now(clock)),
    updatedAt = Timestamp.from(Instant.now(clock)),
    coverAlbum = Some(album1.copy(photoCount = 1))
  )

  val comment1 = Comment(
    id = 1L,
    photo = photo1,
    author = "Kwabena Aning",
    comment = "This is a robust API",
    createdAt = Timestamp.from(Instant.now(clock)),
    approved = true)

  val comment2: Comment =
    comment1.copy(id = 2L, photo = photo2, comment = "This is a second comment")

  val user1 = User(
    id = 1L,
    password = Password("testess"),
    lastLogin = Timestamp.from(Instant.now(clock)),
    isSuperuser = true,
    email = "kwabena.aning@gmail.com",
    firstName = "Kwabena",
    lastName = "Aning",
    isActive = true,
    isStaff = true,
    dateJoined = Timestamp.from(Instant.now(clock))
  )

  val favourite1 = Favourite(photo = photo1, user = user1, createdAt = Timestamp.from(Instant.now(clock)))

  val contact1 = Contact(
    id = 1L,
    firstName = "Kwabena",
    lastName = "Aning",
    email = "kwabena.aning@gmail.com",
    telephone = "12304985004",
    owner = 1L)

  val resort1 = Hospitality(
    id = 1L,
    name = "Some resort",
    featured = false,
    published = true,
    hospitalityType = Resort,
    description = "A new resort of testing purposes",
    address = "Middle of nowhere",
    website = "http://mokocharlie.com",
    dateAdded = Timestamp.from(Instant.now(clock)),
    contact = contact1
  )

  val story1 = Story(
    id = 1L,
    name = "Some photo story",
    description = "A description of a photo story",
    createdAt = Timestamp.from(Instant.now(clock)),
    published = true,
    album = album1
  )
}
