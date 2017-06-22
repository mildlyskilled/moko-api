package com.mokocharlie

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer, Supervision}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

trait CoreServices extends GenericServices with StrictLogging {
  implicit val system: ActorSystem = ActorSystem()
  val decider: Supervision.Decider = {
    case _ ⇒ Supervision.Resume
  }
  implicit val mat: Materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )
  implicit val ec: ExecutionContext = system.dispatcher

  sys.addShutdownHook {
    system.terminate()
  }
}