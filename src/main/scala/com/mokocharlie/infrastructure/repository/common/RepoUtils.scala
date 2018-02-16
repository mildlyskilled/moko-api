package com.mokocharlie.infrastructure.repository.common

trait RepoUtils {
  def offset(page: Int, limit: Int): Int = if (page <= 1) 0 else ((page - 1) * limit) + 1
}
