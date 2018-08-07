package com.mokocharlie.infrastructure

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.scalalogging.StrictLogging

object ActorMaterializerFactory extends StrictLogging {

  private val decider : Supervision.Decider = {
    case e: Exception =>
      logger.error("Resuming after exception", e)
      Supervision.Resume
    case t =>
      logger.error("Stopping after error", t)
      Supervision.Stop
  }

  def createMaterializer(system: ActorSystem) =
    ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))(system)
}