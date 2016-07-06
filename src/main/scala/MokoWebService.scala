import akka.http.scaladsl.Http
import com.mokocharlie.CoreServices
import com.mokocharlie.routing.CoreRoutes

import scala.io.StdIn

object MokoWebService extends App
  with CoreServices {

  val (host, port) = ("localhost", 8080)
  val bindingFuture = Http().bindAndHandle(CoreRoutes.routes, host, port)

  bindingFuture.onFailure {
    case ex: Exception => println(s"Could not bind to $host:$port")
  }

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
