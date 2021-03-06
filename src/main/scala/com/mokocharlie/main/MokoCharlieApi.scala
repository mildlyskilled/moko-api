package com.mokocharlie.main

import akka.http.scaladsl.Http
import com.mokocharlie.incoming.CoreServices
import com.mokocharlie.infrastructure.inbound.routing.CoreRoutes
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.io.StdIn

object MokoCharlieApi extends App with CoreServices with StrictLogging {

  val config = ConfigFactory.load()
  val (host, port) = (config.getString("mokocharlie.http.host"), config.getInt("mokocharlie.http.port"))
  val bindingFuture = Http().bindAndHandle(new CoreRoutes(ConfigFactory.load()).routes, host, port)

  bindingFuture.failed.foreach {
    case ex: Exception => logger.error(s"Could not bind to $host:$port", ex)
  }

  println(s"Server online at http://$host:$port/\nType 'shutdown' to stop...")
  Iterator.continually(StdIn.readLine).takeWhile(_ != "shutdown").foreach{ command => 
      if ( command != null) println(s"$command not recognised")
  }

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
