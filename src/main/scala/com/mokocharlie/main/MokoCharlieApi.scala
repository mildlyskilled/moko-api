package com.mokocharlie.main

import java.time.Clock

import akka.http.scaladsl.Http
import com.mokocharlie.infrastructure.inbound.routing.CoreRoutes
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.io.StdIn

object MokoCharlieApi extends App with CoreServices with StrictLogging {

  val config = ConfigFactory.load()
  val clock = Clock.systemUTC()
  val (host, port) = (config.getString("mokocharlie.http.host"), config.getInt("mokocharlie.http.port"))
  val bindingFuture = Http().bindAndHandle(new CoreRoutes(ConfigFactory.load(), clock).routes, host, port)

  bindingFuture.failed.foreach {
    case ex: Exception => logger.error(s"Could not bind to $host:$port", ex)
  }

  println(s"Server online at http://$host:$port/\nType 'shutdown' to stop...")
  logger.info(s"Server online at http://$host:$port")
  Iterator.continually(StdIn.readLine).takeWhile(_ != "shutdown").foreach{ command ⇒
      if ( command != null) println(s"$command not recognised")
  }

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done

}
