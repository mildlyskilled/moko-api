package com.mokocharlie.domain

import scala.collection.immutable.Seq

case class Page[+A](items: Seq[A], page: Int, offset: Int, tot: Option[Int]) {
  val total: Int = tot match {
    case Some(t) => t
    case None    => 0
  }
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
  lazy val size: Int = total
  def isEmpty: Boolean = items.isEmpty
}
