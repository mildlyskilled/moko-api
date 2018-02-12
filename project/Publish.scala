import sbt.Keys._
import scala.language.postfixOps

object Publish {

  lazy val noop = Seq(
    publish       := (),
    publishLocal  := ()
  )

  lazy val settings = Seq(
    crossPaths        :=  false,
    publishMavenStyle :=  false
  )
}
