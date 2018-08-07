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
      .map(_ ⇒ hc.healthCheck).log("HEALTHCHECK")
      .mapAsync(1)(res ⇒
        res.map {
          case Right(status) ⇒
            val checkStatus = if (status) "success" else "failure"
            s"Connection check resulted in [$checkStatus]"
          case Left(ex) ⇒
            s"Connection check failed [${ex.msg}]"
      }).log("HEALTHCHECK")
      .runForeach(s ⇒ logger.info(s))
}
