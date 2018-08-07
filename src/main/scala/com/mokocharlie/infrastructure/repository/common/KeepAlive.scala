package com.mokocharlie.infrastructure.repository.common

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.mokocharlie.service.HealthCheckService
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.duration.FiniteDuration


class KeepAlive(hc: HealthCheckService)(implicit val materializer: Materializer) extends StrictLogging {

  def run(delay: FiniteDuration, interval: FiniteDuration): Unit = {
    Source.tick(delay, interval, ())
      .map(_ ⇒ hc.healthCheck)
      .runForeach(res ⇒ logger.info(s"$res"))
  }
}
