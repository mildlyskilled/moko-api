package com.mokocharlie.converter

import java.sql.Timestamp

import com.mokocharlie.model.Password
import spray.json.{DeserializationException, JsNumber, JsString, JsValue, RootJsonFormat}

object Conversions {
  implicit object TimestampFormat extends RootJsonFormat[Timestamp] {

    def write(obj: Timestamp) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsNumber(time) => new Timestamp(time.toLong)

      case _ => throw DeserializationException("Date expected")
    }
  }

  implicit object PasswordFormat extends RootJsonFormat[Password] {
    def write(obj: Password) = JsString(obj.toString)

    def read(value: JsValue) = {
      value.asJsObject.getFields("value") match {
        case Seq(JsString(v)) => Password(v)
        case _ => throw DeserializationException("Password expected")
      }
    }
  }
}