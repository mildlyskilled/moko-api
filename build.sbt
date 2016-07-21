name := "mokocharlie-api"

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.mokocharlie"

lazy val root = (project in file(".")).
  enablePlugins(JavaServerAppPackaging).
  settings(
    name := "mokocharlie-api",
    scalaVersion := "2.11.8",
    version := "1.0"
  )

libraryDependencies ++= {
  lazy val akkaVersion = "2.4.7"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"  % akkaVersion,
    "com.typesafe.slick" %% "slick" % "3.1.1",
    "mysql" % "mysql-connector-java" % "5.1.38",
    "com.mchange" % "c3p0" % "0.9.5.2",
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.1.3",
    "ch.megard" %% "akka-http-cors" % "0.1.4",
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion   % "test",
    "org.scalatest"     %% "scalatest"       % "2.2.0"       % "test"
  )
}