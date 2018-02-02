package com.mokocharlie.infrastructure.outbound

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.RequestEntity.AuthRequest
import com.mokocharlie.domain.{Page, Password, Token}
import spray.json._

trait JsonConversion extends DefaultJsonProtocol {
  implicit object TimestampFormat extends RootJsonFormat[Timestamp] {

    def write(obj: Timestamp) = JsString(obj.toString)

    def read(json: JsValue): Timestamp = json match {
      case JsNumber(time) => new Timestamp(time.toLong)

      case _ => throw DeserializationException("Date expected")
    }
  }

  implicit object PasswordFormat extends RootJsonFormat[Password] {
    def write(obj: Password) = JsString(obj.toString)

    def read(value: JsValue): Password = {
      value.asJsObject.getFields("value") match {
        case Seq(JsString(v)) ⇒ Password(v)
        case _ ⇒ throw DeserializationException("Password expected")
      }
    }
  }

  // formats for unmarshalling and marshalling
  implicit val photoFormat = jsonFormat11(Photo)
  implicit val albumFormat = jsonFormat9(Album)
  implicit val commentFormat = jsonFormat6(Comment)
  implicit val favouriteFormat = jsonFormat4(Favourite)
  implicit val userFormat = jsonFormat10(User)
  implicit val collectionFormat = jsonFormat8(Collection)
  implicit val videoFormat = jsonFormat3(Video)
  implicit val documentaryFormat = jsonFormat5(Documentary)
  implicit val tokenFormat = jsonFormat4(Token)
  // Request Serialisers
  implicit val authRequestFormat = jsonFormat2(AuthRequest)

  implicit val photoPageFormat = jsonFormat(Page[Photo], "items", "page", "offset", "total")
  implicit val albumPageFormat = jsonFormat(Page[Album], "items", "page", "offset", "total")
  implicit val commentPageFormat = jsonFormat(Page[Comment], "items", "page", "offset", "total")
  implicit val favouritePageFormat = jsonFormat(Page[Favourite], "items", "page", "offset", "total")
  implicit val collectionPageFormat =
    jsonFormat(Page[Collection], "items", "page", "offset", "total")
  implicit val videoPageFormat = jsonFormat(Page[Video], "items", "page", "offset", "total")
  implicit val documentaryPageFormat =
    jsonFormat(Page[Documentary], "items", "page", "offset", "total")
}
