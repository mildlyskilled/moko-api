package com.mokocharlie.model


import java.sql.Timestamp

case class Album(
                  id: Long,
                  albumId: Option[Long],
                  label: String,
                  description: String,
                  coverId: Option[Long],
                  createdAt: Timestamp,
                  updatedAt: Option[Timestamp],
                  published: Boolean,
                  featured: Boolean)
