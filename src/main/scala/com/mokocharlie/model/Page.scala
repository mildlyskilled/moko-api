package com.mokocharlie.model

case class Page[+A](items: Seq[A], page: Int, offset: Int, total: Option[Int])  {
  val ttotal = total match {
    case Some(t) => t
    case None => 0
  }
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < ttotal)
}
