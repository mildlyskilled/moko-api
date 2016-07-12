package com.mokocharlie.model

import java.sql.Timestamp

case class Comment(commentID: Long,
                   imageId: Long,
                   comment: String,
                   author: String,
                   createdAt: Timestamp,
                   approved: Boolean)