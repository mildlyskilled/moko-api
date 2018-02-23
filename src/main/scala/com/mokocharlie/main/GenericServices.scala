package com.mokocharlie.main

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

trait GenericServices {
    implicit def system: ActorSystem

    implicit def ec: ExecutionContext

    implicit def mat: Materializer
}
