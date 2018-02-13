import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import sbt.Keys._
import sbt._

object  MyBuild extends Build {
  lazy val httpVersion = "10.0.11"

  lazy val commonSettings = Seq(
    scalaVersion := "2.12.4",
    organization := "com.mokocharlie",
    name := "mokocharlie-api",
    version := "1.0"
  )

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

  lazy val akkaTestKit = "com.typesafe.akka" %% "akka-http-testkit" % httpVersion

  lazy val root = (project in file("."))
    .configs(IntegrationTest)
    .enablePlugins(JavaServerAppPackaging)
    .settings(
      commonSettings,
      Defaults.itSettings,
      libraryDependencies ++=
        Seq(
          scalaTest % "it,test",
          akkaTestKit % "it,test"
        )
      // other settings here
    )
}
