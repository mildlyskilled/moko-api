package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

import scala.concurrent.Future

trait MokoCharlieService {
  def system: ActorSystem
  private val dbExecutionContext = system.dispatchers.lookup("mokocharlie.db-dispatcher")

  protected def dbExecute[T](f: ⇒ T): Future[T] = Future(f)(dbExecutionContext)

  def publishedFlag(user: RepositoryResponse[User]): Option[Boolean] =
    user.map(_.isSuperuser) match {
      case Right(superuser) if superuser ⇒ None
      case _ ⇒ Some(true)
    }

  def publishedFlag(user: User): Option[Boolean] =
    if (user.isSuperuser) None else Some(true)
}
