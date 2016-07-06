package com.mokocharlie

import com.mokocharlie.model.{Album, Page, Photo}
import spray.json.DefaultJsonProtocol._
import com.mokocharlie.converter.Conversions._

trait Marshalling {
  // formats for unmarshalling and marshalling
  implicit val photoFormat = jsonFormat11(Photo)
  implicit val albumFormat = jsonFormat9(Album)
  implicit val photoPageFormat = jsonFormat(Page[Photo], "items", "page", "offset", "total")
  implicit val albumPageFormat = jsonFormat(Page[Album], "items", "page", "offset", "total")
}