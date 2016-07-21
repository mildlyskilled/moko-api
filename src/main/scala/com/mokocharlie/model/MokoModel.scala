package com.mokocharlie.model

import java.sql.Timestamp

sealed trait MokoModel

final case class Album (
                  id: Long,
                  albumId: Option[Long],
                  label: String,
                  description: String,
                  coverId: Option[Long],
                  createdAt: Timestamp,
                  updatedAt: Option[Timestamp],
                  published: Boolean,
                  featured: Boolean) extends MokoModel


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
                 cloudImage: Option[String]) extends MokoModel

final case class PhotoAlbum(id: Long, photo: Long, album: Long) extends MokoModel

final case class User(id: Long,
                      password: String,
                      lastLogin: Timestamp,
                      isSuperuser: Boolean,
                      email: String,
                      firstName: String,
                      lastName: String,
                      isStaff: Boolean,
                      isActive: Boolean,
                      dateJoined: Timestamp) extends MokoModel

final case class Favourite(id: Long, photoID: Long, userID: Long, createdAt: Timestamp) extends MokoModel

final case class Comment(commentID: Long,
                         imageId: Long,
                         comment: String,
                         author: String,
                         createdAt: Timestamp,
                         approved: Boolean) extends MokoModel