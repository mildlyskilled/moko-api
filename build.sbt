name := "mokocharlie-api"

version := "1.0"

scalaVersion := "2.12.4"

organization := "com.mokocharlie"

lazy val root = (project in file(".")).
  enablePlugins(JavaServerAppPackaging).
  settings(
    name := "mokocharlie-api",
    scalaVersion := "2.12.4",
    version := "1.0"
  )

libraryDependencies ++= {
  lazy val akkaVersion = "2.5.8"
  lazy val httpVersion = "10.0.11"
  lazy val catsVersion = "0.9.0"
  lazy val logbackVersion = "1.1.3"
  lazy val scalikeVersion = "2.5.0"

  Seq(
    // typesafe
    "com.typesafe.akka" %% "akka-http" % httpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % httpVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "org.scalikejdbc" %% "scalikejdbc" % scalikeVersion,
    "org.scalikejdbc" %% "scalikejdbc-jsr310" % scalikeVersion,

    // database layer
    "mysql" % "mysql-connector-java" % "5.1.42",
    "com.mchange" % "c3p0" % "0.9.5.2",

    // logging
    "ch.qos.logback" % "logback-classic" % logbackVersion % "runtime",
    "ch.qos.logback" % "logback-core" % logbackVersion % "runtime",
    "org.codehaus.janino" % "janino" % "2.7.8",

    //other 3rd party
    "org.typelevel" %% "cats" % catsVersion,

    // Test dependencies
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % httpVersion % "test"
  )
}
enablePlugins(JavaAppPackaging)
mainClass in assembly := Some("com.mokocharlie.main.MokoCharlieApi")

enablePlugins(DockerPlugin)
dockerBaseImage       := "openjdk:jre-alpine"

enablePlugins(AshScriptPlugin)