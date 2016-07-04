package com.mokocharlie.model

import java.sql.Timestamp

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
                 cloudImage: Option[String])
