package com.mokocharlie.infrastructure.outbound

import java.sql.Timestamp

import com.mokocharlie.domain.MokoModel._
import com.mokocharlie.domain.common.MokoCharlieServiceError
import com.mokocharlie.domain.common.MokoCharlieServiceError.APIError
import com.mokocharlie.domain.common.RequestEntity.{AuthRequest, CommentRequest, FavouriteRequest}
import com.mokocharlie.domain._
import spray.json._

trait JsonConversion extends DefaultJsonProtocol {
  implicit object TimestampFormat extends RootJsonFormat[Timestamp] {

    def write(obj: Timestamp) = JsString(obj.toString)

    def read(json: JsValue): Timestamp = json match {
      case JsNumber(time) => new Timestamp(time.toLong)
      case JsString(timestamp) ⇒ Timestamp.valueOf(timestamp)
      case ex => throw DeserializationException(s"Date expected got $ex")
    }
  }

  implicit object FavouriteFormat extends RootJsonFormat[FavouriteRequest] {
    def read(value: JsValue): FavouriteRequest = {
      value.asJsObject.getFields("userId", "photoId") match {
        case Seq(JsNumber(uid), JsNumber(pid)) ⇒ FavouriteRequest(uid.toLong, pid.toLong)
        case _ ⇒ throw DeserializationException(s"malformed favourite payload $value")
      }
    }

    def write(obj: FavouriteRequest): JsValue = {
      JsObject(Map("userId" → JsNumber(obj.userId), "photoId" → JsNumber(obj.photoId)))
    }
  }

  implicit object HospitalityTypeFormat extends RootJsonFormat[HospitalityType] {
    override def read(json: JsValue): HospitalityType = HospitalityType.apply(json.toString)

    override def write(obj: HospitalityType): JsValue = JsString(obj.value)
  }

  implicit object APIErrorFormat extends RootJsonFormat[APIError] {
    override def write(obj: APIError): JsValue =
      JsObject(
        Map(
          "error" → obj.code.value.toJson,
          "message" → obj.msg.toJson
        )
      )

    override def read(json: JsValue): APIError =
      deserializationError(s"Deserialisation not supported, $json")
  }

  implicit object HealthCheckFormat extends RootJsonFormat[HealthCheck] {
    override def read(json: JsValue): HealthCheck = {
      val components = json.asJsObject.getFields("components")
        .map(x ⇒ x.asJsObject.fields)
        .headOption
        .map{ components ⇒
          components.keys.zip(components.values.map(_.convertTo[String])).toMap
        }.getOrElse(Map.empty)
      HealthCheck(components)
    }


    override def write(obj: HealthCheck): JsValue = {
      val status = obj.components.values.count(_ == "OK") match {
        case c if c == obj.components.size ⇒ "Fully Operational"
        case c if c < obj.components.size && c > 0 ⇒ "Partially Operational"
        case c if c == 0 ⇒ "Complete failure"
        case _ ⇒ "Nominal"
      }

      JsObject(
        Map(
          "status" → status.toJson,
          "components" → obj.components.toJson
        )
      )
    }

  }
  // formats for unmarshalling and marshalling
  implicit val photoFormat = jsonFormat13(Photo)
  implicit val albumFormat = jsonFormat10(Album)
  implicit val commentFormat = jsonFormat6(Comment)
  implicit val passwordFormat = jsonFormat1(Password)
  implicit val userFormat = jsonFormat10(User)
  implicit val favouriteFormat = jsonFormat4(Favourite)
  implicit val collectionFormat = jsonFormat8(Collection)
  implicit val videoFormat = jsonFormat3(Video)
  implicit val documentaryFormat = jsonFormat5(Documentary)
  implicit val tokenFormat = jsonFormat4(Token)
  implicit val contactFormat = jsonFormat6(Contact)
  implicit val hospitalityFormat = jsonFormat11(Hospitality)
  implicit val storyFormat = jsonFormat6(Story)

  // Request Serialisers
  implicit val authRequestFormat = jsonFormat2(AuthRequest)
  implicit val commentRequestFormat = jsonFormat3(CommentRequest)

  implicit val photoPageFormat = jsonFormat(Page[Photo], "items", "page", "offset", "total")
  implicit val albumPageFormat = jsonFormat(Page[Album], "items", "page", "offset", "total")
  implicit val commentPageFormat = jsonFormat(Page[Comment], "items", "page", "offset", "total")
  implicit val favouritePageFormat = jsonFormat(Page[Favourite], "items", "page", "offset", "total")
  implicit val collectionPageFormat =
    jsonFormat(Page[Collection], "items", "page", "offset", "total")
  implicit val videoPageFormat = jsonFormat(Page[Video], "items", "page", "offset", "total")
  implicit val documentaryPageFormat =
    jsonFormat(Page[Documentary], "items", "page", "offset", "total")
  implicit val hospitalityPageFormat = jsonFormat(Page[Hospitality], "items", "page", "offset", "total")
  implicit val storyPageFormat = jsonFormat(Page[Story], "items", "page", "offset", "total")
}

object JsonConversion extends JsonConversion {}
