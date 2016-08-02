package com.mokocharlie

import com.mokocharlie.model._
import spray.json.DefaultJsonProtocol._
import com.mokocharlie.converter.Conversions._
import spray.json.JsonFormat

trait Marshalling {
  // formats for unmarshalling and marshalling
  implicit val photoFormat = jsonFormat11(Photo)
  implicit val albumFormat = jsonFormat9(Album)
  implicit val commentFormat = jsonFormat6(Comment)
  implicit val favouriteFormat = jsonFormat4(Favourite)
  implicit val userFormat = jsonFormat10(User)
  implicit val collectionFormat = jsonFormat7(Collection)
  implicit val photoPageFormat = jsonFormat(Page[Photo], "items", "page", "offset", "total")
  implicit val albumPageFormat = jsonFormat(Page[Album], "items", "page", "offset", "total")
  implicit val commentPageFormat = jsonFormat(Page[Comment], "items", "page", "offset", "total")
  implicit val favouritePageFormat = jsonFormat(Page[Favourite], "items", "page", "offset", "total")
  implicit val collectionPageFormat = jsonFormat(Page[Collection], "items", "page", "offset", "total")
}
