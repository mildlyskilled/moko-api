import sbt._
import sbt.Keys._
import scala.collection.immutable.Seq

object Settings {

  private lazy val general = Seq(
    version <<= version in ThisBuild,
    scalaVersion := "2.12.4",
    organization := "com.mokocharlie",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfuture", "-Xlint", "-Ypartial-unification"),
    incOptions := incOptions.value.withNameHashing(true),
    doc in Compile <<= target.map(_ / "none")
  )

  private val mokocharlieSettings = Seq(
    mainClass in (Compile, run) := Some("com.mokocharlie.main.MokoCharlieApi")
  )
  private lazy val shared = general ++ Testing.settings ++ Dependencies.resolvers

  lazy val root     = shared ++ Publish.noop
  lazy val testkit  = shared ++ Publish.noop     ++ Dependencies.testKit
  lazy val core = shared ++ Publish.settings ++ Dependencies.core
  lazy val moko = shared ++ Publish.settings ++ mokocharlieSettings
}
