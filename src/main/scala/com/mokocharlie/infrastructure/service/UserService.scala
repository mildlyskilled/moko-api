package com.mokocharlie.infrastructure.service

import akka.actor.ActorSystem
import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.common.ServiceResponse.ServiceResponse
import com.mokocharlie.infrastructure.repository.UserRepository

class UserService(repo: UserRepository)(implicit override val system: ActorSystem)
    extends MokoCharlieService {

  def createOrUpdate(user: User): ServiceResponse[Long] =
    dbExecute {
      repo
        .userById(user.id)
        .map(repo.update)
        .getOrElse(repo.create(user))
    }

  def userById(userId: Long): ServiceResponse[User] =
    dbExecute(repo.userById(userId))
}
