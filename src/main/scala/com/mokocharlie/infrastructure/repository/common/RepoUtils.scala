package com.mokocharlie.infrastructure.repository.common

trait RepoUtils {
  def rowCount(page: Int, limit: Int): Int = (page * limit) + 1
  def dbPage(page: Int): Int = if (page > 0) page - 1 else page
}
