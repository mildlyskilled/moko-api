package com.mokocharlie.infrastructure.repository.common

trait RepoUtils {
  def offset(page: Int, limit: Int): Int = (page * limit) + 1
}
