package com.mokocharlie

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import scala.concurrent.ExecutionContext

trait GenericServices {
    implicit def system: ActorSystem

    implicit def log: LoggingAdapter

    implicit def ec: ExecutionContext

    implicit def mat: Materializer
}
