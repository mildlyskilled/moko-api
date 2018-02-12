import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer, Supervision}
import com.mokocharlie.incoming.CoreServices
import com.mokocharlie.infrastructure.inbound.routing.CoreRoutes
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

trait HttpScaffold extends StrictLogging {

  implicit val system: ActorSystem = ActorSystem("test-http-server")
  val decider: Supervision.Decider = _ ⇒ Supervision.Resume
  implicit val mat: Materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )
  implicit val ec: ExecutionContext = system.dispatcher

  val config: Config = ConfigFactory.load()
  val clock: Clock = Clock.systemUTC()
  val (host, port) = (config.getString("mokocharlie.http.host"), config.getInt("mokocharlie.http.port"))

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(new CoreRoutes(ConfigFactory.load(), clock).routes, host, port)

  def start(): Unit = {

    bindingFuture.failed.foreach {
      case ex: Exception => logger.error(s"Could not bind to $host:$port", ex)
    }


    println(s"Test server online at http://$host:$port/\nType 'shutdown' to stop...")
    Iterator.continually(StdIn.readLine).takeWhile(_ != "shutdown").foreach{ command ⇒
      if ( command != null) println(s"$command not recognised")
    }
  }

  def stop(): Unit =
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  def sendRequest(path: String) =
    Http().singleRequest(HttpRequest(uri = path))
}
