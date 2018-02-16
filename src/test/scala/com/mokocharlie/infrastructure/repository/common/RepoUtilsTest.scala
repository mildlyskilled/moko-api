package com.mokocharlie.infrastructure.repository.common

import org.scalatest.{FlatSpec, Matchers}

class RepoUtilsTest extends FlatSpec with Matchers {

  val util = new RepoUtils {}

  "Repo util" should "paginate properly" in {
    util.offset(0, 20) shouldBe 0
    util.offset(1, 10) shouldBe 0
    util.offset(2, 10) shouldBe 11
    util.offset(3, 10) shouldBe 21
  }
}
