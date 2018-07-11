package com.mokocharlie.infrastructure.repository

import com.mokocharlie.domain.MokoModel.User
import com.mokocharlie.domain.Page
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse

trait UserRepository {

  def user(id: Long): RepositoryResponse[User]

  def user(email: String): RepositoryResponse[User]

  def userByToken(token: String): RepositoryResponse[User]

  def update(user: User): RepositoryResponse[Long]

  def create(user: User): RepositoryResponse[Long]

  def list(page: Int, limit: Int): RepositoryResponse[Page[User]]

  def changePassword(
      id: Long,
      currentPassword: String,
      newPassword: String): RepositoryResponse[Long]

}
