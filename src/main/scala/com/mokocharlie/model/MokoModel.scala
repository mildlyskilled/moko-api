package com.mokocharlie.model

import java.sql.Timestamp

import slick.lifted.MappedTo

sealed trait MokoModel

final case class Album(
                        id: Long,
                        albumId: Option[Long],
                        label: String,
                        description: String,
                        coverId: Option[Long],
                        createdAt: Timestamp,
                        updatedAt: Option[Timestamp],
                        published: Boolean,
                        featured: Boolean
                      ) extends MokoModel


final case class Photo(id: Long,
                       imageId: String,
                       name: String,
                       path: Option[String],
                       caption: String,
                       createdAt: Timestamp,
                       updatedAt: Timestamp,
                       ownerId: Long,
                       published: Boolean,
                       deletedAt: Option[Timestamp],
                       cloudImage: Option[String]
                      ) extends MokoModel

final case class PhotoAlbum(id: Long, photo: Long, album: Long) extends MokoModel

final case class User(id: Long,
                      password: Password,
                      lastLogin: Timestamp,
                      isSuperuser: Boolean,
                      email: String,
                      firstName: String,
                      lastName: String,
                      isStaff: Boolean,
                      isActive: Boolean,
                      dateJoined: Timestamp
                     ) extends MokoModel

final case class Favourite(id: Long, photoID: Long, userID: Long, createdAt: Timestamp) extends MokoModel

final case class Comment(commentID: Long,
                         imageId: Long,
                         comment: String,
                         author: String,
                         createdAt: Timestamp,
                         approved: Boolean
                        ) extends MokoModel

final case class Collection(id: Long,
                            name: String,
                            featured: Boolean,
                            createdAt: Timestamp,
                            updatedAt: Timestamp,
                            description: String,
                            coverAlbumId: Long
                           ) extends MokoModel


final case class CollectionAlbum(id: Long,
                                 collectionId: Long,
                                 albumId: Long
                                ) extends MokoModel

final case class Video(id: Long, externalId: String, externalSource: String) extends MokoModel

final case class Documentary(id: Long, description: String, status: String, createdAt: Timestamp, updatedAt: Timestamp)
  extends MokoModel

case class Password(value: String) extends MappedTo[String] {
  override def toString: String = "<redacted>"
}