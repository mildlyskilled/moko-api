name := "mokocharlie-api"

version := "1.0"

scalaVersion := "2.12.2"

organization := "com.mokocharlie"

lazy val root = (project in file(".")).
  enablePlugins(JavaServerAppPackaging).
  settings(
    name := "mokocharlie-api",
    scalaVersion := "2.12.2",
    version := "1.0"
  )

libraryDependencies ++= {
  lazy val akkaVersion = "2.4.17"
  lazy val httpVersion = "10.0.7"
  lazy val catsVersion = "0.9.0"
  lazy val logbackVersion = "1.1.3"
  lazy val slickVersion = "3.2.0"
  lazy val corsVersion = "0.2.1"

  Seq(
    // typesafe
    "com.typesafe.akka" %% "akka-http" % httpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % httpVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json"  % akkaVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,

    // database layer
    "mysql" % "mysql-connector-java" % "5.1.42",
    "com.mchange" % "c3p0" % "0.9.5.2",

    "ch.qos.logback" % "logback-classic" % logbackVersion % "runtime",
    "ch.qos.logback" % "logback-core" % logbackVersion % "runtime",
    "net.logstash.logback" % "logstash-logback-encoder" % "4.6",
    "org.codehaus.janino" % "janino" % "2.7.8",

    "ch.megard" %% "akka-http-cors" % corsVersion,

    //other 3rd party
    "org.typelevel" %% "cats" % catsVersion,

    // Test dependencies
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % httpVersion % "test"
  )
}