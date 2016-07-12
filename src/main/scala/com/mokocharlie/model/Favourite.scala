package com.mokocharlie.model

import java.sql.Timestamp

case class Favourite(id: Long, photoID: Long, userID: Long, createdAt: Timestamp)
