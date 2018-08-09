package com.mokocharlie.infrastructure.inbound.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import com.mokocharlie.service.{StoryService, UserService}
import akka.http.scaladsl.server.Directives._
import com.mokocharlie.infrastructure.security.HeaderChecking
import com.mokocharlie.infrastructure.outbound.JsonConversion

import scala.concurrent.ExecutionContext

class StoryRouting(storyService: StoryService, override val userService: UserService)(implicit system: ActorSystem)
    extends SprayJsonSupport
    with HttpUtils
    with HeaderChecking
    with JsonConversion {

  implicit val ec: ExecutionContext = system.dispatcher

  val routes: Route = {
    path("stories" ~ Slash.?) {
      parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) ⇒
        optionalHeaderValue(extractUserToken){ tokenResponse ⇒
          tokenResponse.map{ token ⇒
            val res = for {
              u ← token.user
              stories ← storyService.list(page, limit, userService.publishedFlag(u))
            } yield stories

            onSuccess(res){
              case Right(storyPage) ⇒ complete(storyPage)
              case Left(error) ⇒ completeWithError(error)
            }
          }.getOrElse {
            onSuccess(storyService.list(page, limit, Some(true))) {
              case Right(storyPage) ⇒ complete(storyPage)
              case Left(error) ⇒ completeWithError(error)
            }
          }
        }
      }

    } ~ path("stories" / LongNumber ~ Slash.?) { id ⇒
      onSuccess(storyService.storyById(id)) {
        case Right(story) ⇒ complete(story)
        case Left(error) ⇒ completeWithError(error)
      }
    }
  }
}
