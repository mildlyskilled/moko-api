package com.mokocharlie.domain

import java.sql.Timestamp

import scalikejdbc._

sealed trait MokoModel

object MokoModel {

  final case class Album(
      id: Option[Long] = Some(0L),
      albumId: Option[Long],
      label: String,
      description: String,
      cover: Option[Photo],
      createdAt: Timestamp,
      updatedAt: Option[Timestamp],
      published: Boolean,
      featured: Boolean,
      photoCount: Int)
      extends MokoModel

  final case class Hospitality(
      id: Long = 0L,
      name: String,
      featured: Boolean,
      hospitalityType: HospitalityType,
      description: String,
      address: String,
      website: String,
      dateAdded: Timestamp,
      published: Boolean,
      contact: Contact,
      album: Album
  ) extends MokoModel

  final case class Contact(
      id: Long,
      firstName: String,
      lastName: String,
      email: String,
      telephone: String,
      owner: Long
  ) extends MokoModel

  final case class Photo(
      id: Long = 0L,
      imageId: Option[String],
      name: String,
      path: Option[String],
      caption: String,
      createdAt: Timestamp,
      updatedAt: Option[Timestamp],
      ownerId: Long,
      published: Boolean,
      deletedAt: Option[Timestamp],
      cloudImage: Option[String],
      commentCount: Int,
      favouriteCount: Int)
      extends MokoModel

  final case class PhotoAlbum(id: Long, photo: Long, album: Long) extends MokoModel

  final case class User(
      id: Long = 0L,
      password: Password,
      lastLogin: Timestamp,
      isSuperuser: Boolean,
      email: String,
      firstName: String,
      lastName: String,
      isStaff: Boolean,
      isActive: Boolean,
      dateJoined: Timestamp)
      extends MokoModel

  final case class Favourite(id: Long = 0L, photo: Photo, user: User, createdAt: Timestamp)
      extends MokoModel

  final case class Comment(
      id: Long = 0L,
      photo: Photo,
      comment: String,
      author: String,
      createdAt: Timestamp,
      approved: Boolean)
      extends MokoModel

  final case class Collection(
      id: Long,
      name: String,
      featured: Boolean,
      createdAt: Timestamp,
      updatedAt: Timestamp,
      description: String,
      coverAlbum: Option[Album],
      published: Boolean)
      extends MokoModel

  final case class CollectionAlbum(id: Long, collectionId: Long, albumId: Long) extends MokoModel

  final case class Video(id: Long, externalId: String, externalSource: String) extends MokoModel

  final case class Documentary(
      id: Long,
      description: String,
      status: String,
      createdAt: Timestamp,
      updatedAt: Timestamp)
      extends MokoModel

  final case class DocumentaryVideo(id: Long, documentaryID: Long, videoID: Long) extends MokoModel

  final case class Story(
      id: Long = 0L,
      name: String,
      description: String,
      createdAt: Timestamp,
      published: Boolean,
      album: Album)
      extends MokoModel

}

final case class Password(value: String) {
  override def toString: String = "<redacted>"
}

final case class Token(value: String, refreshToken: String, userId: Long, expiresAt: Timestamp)

sealed trait HospitalityType {
  def value: String
}

object HospitalityType {
  def apply(hType: String): HospitalityType = hType.toUpperCase match {
    case "HOTEL" ⇒ Hotel
    case _ ⇒ Resort
  }

  def apply(hType: Option[String]): HospitalityType = hType.map(apply).getOrElse(Hotel)

  case object Resort extends HospitalityType {
    override def value: String = "RESORT"
  }

  case object Hotel extends HospitalityType {
    override def value: String = "HOTEL"
  }

}

case class HealthCheck(components: Map[String, String])

object HealthCheck {
  def apply(components: Map[String, String]): HealthCheck = new HealthCheck(components)
}
