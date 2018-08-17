package com.mokocharlie.infrastructure.security

object RandomStringUtil {
  def randomStringRecursive(n: Int): String = {
    n match {
      case 1 => util.Random.nextPrintableChar.toString
      case _ => util.Random.nextPrintableChar + randomStringRecursive(n-1)
    }
  }
}
