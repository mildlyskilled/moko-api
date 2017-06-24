import akka.http.scaladsl.Http
import com.mokocharlie.incoming.CoreServices
import com.mokocharlie.infrastructure.inbound.routing.CoreRoutes
import com.typesafe.scalalogging.StrictLogging

import scala.io.StdIn

object Main extends App with CoreServices with StrictLogging {

  val (host, port) = ("localhost", 8080)
  val bindingFuture = Http().bindAndHandle(CoreRoutes.routes, host, port)

  bindingFuture.failed.foreach {
    case ex: Exception => logger.error(s"Could not bind to $host:$port", ex)
  }

  println(s"Server online at http://$host:$port/\nType 'shutdown' to stop...")
  Iterator.continually(StdIn.readLine).takeWhile(_ != "shutdown").foreach { command =>
    println(s"$command not recognised")
  }

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
