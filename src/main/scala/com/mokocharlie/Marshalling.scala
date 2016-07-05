package com.mokocharlie

import com.mokocharlie.model.{Page, Photo}
import spray.json.DefaultJsonProtocol._
import com.mokocharlie.converter.Conversions._

trait Marshalling {
  // formats for unmarshalling and marshalling
  implicit val photoFormat = jsonFormat11(Photo)
  implicit val photoPageFormat = jsonFormat(Page[Photo], "items", "page", "offset", "total")
}
