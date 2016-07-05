import akka.http.scaladsl.Http
import com.mokocharlie.{CoreServices, Routing}

import scala.io.StdIn

object WebServer extends App with CoreServices with Routing{

  val (host, port) = ("localhost", 8080)
  val bindingFuture = Http().bindAndHandle(routes, host, port)

  bindingFuture.onFailure {
    case ex: Exception => println(s"Could not bind to $host:$port")
  }

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
