package com.mokocharlie.converter

import java.sql.Timestamp

import spray.json.{DeserializationException, JsNumber, JsString, JsValue, RootJsonFormat}

object Conversions {
  implicit object TimestampFormat extends RootJsonFormat[Timestamp] {

    def write(obj: Timestamp) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsNumber(time) => new Timestamp(time.toLong)

      case _ => throw new DeserializationException("Date expected")
    }
  }
}