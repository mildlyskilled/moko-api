package com.mokocharlie.service

import akka.actor.ActorSystem

import scala.concurrent.Future

trait MokoCharlieService {
  def system: ActorSystem
  private val dbExecutionContext = system.dispatchers.lookup("mokocharlie.db-dispatcher")

  protected def dbExecute[T](f: ⇒ T): Future[T] = Future(f)(dbExecutionContext)
}
