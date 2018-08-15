package com.mokocharlie.infrastructure.repository.common

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.mokocharlie.service.HealthCheckService
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class KeepAlive(hc: HealthCheckService)(
    implicit val materializer: Materializer,
    implicit val ec: ExecutionContext)
    extends StrictLogging {

  def run(delay: FiniteDuration, interval: FiniteDuration): Unit =
    Source
      .tick(delay, interval, ())
      .map(_ ⇒ hc.healthCheck)
      .mapAsync(1)(res ⇒ res)
      .runForeach {
        case Right(_) ⇒ _
        case Left(ex) ⇒
          logger.info(s"Connection check failed [${ex.msg}]")
      }
}
