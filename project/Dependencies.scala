import sbt._
import sbt.Keys._

object Dependencies {

  lazy val akkaVersion = "2.5.8"
  lazy val httpVersion = "10.0.11"
  lazy val logbackVersion = "1.1.3"
  lazy val scalikeVersion = "2.5.0"

  // typesafe
  private val akkaHttp          = "com.typesafe.akka" %% "akka-http" % httpVersion
  private val akkaHttpSpray     = "com.typesafe.akka" %% "akka-http-spray-json" % httpVersion
  private val akkaSl4j          = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  private val akkaStream        = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  private val scalaLogging      = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  private val scalike           = "org.scalikejdbc" %% "scalikejdbc" % scalikeVersion
  private val scalikeJsr        = "org.scalikejdbc" %% "scalikejdbc-jsr310" % scalikeVersion

  // database layer
  private val mysqlConnector    = "mysql" % "mysql-connector-java" % "5.1.42"
  private val c3po              = "com.mchange" % "c3p0" % "0.9.5.2"

  // logging
  private val logBack           = "ch.qos.logback" % "logback-classic" % logbackVersion % "runtime"
  private val logBackCore       = "ch.qos.logback" % "logback-core" % logbackVersion % "runtime"
  private val janino            = "org.codehaus.janino" % "janino" % "2.7.8"

  //other 3rd party
  private val pdkdf2            = "io.github.nremond" %% "pbkdf2-scala" % "0.6.3"

  // Test dependencies
  private val scalaTest         = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  private val akkaTestKit       = "com.typesafe.akka" %% "akka-http-testkit" % httpVersion % "test"

  lazy val resolvers            = Seq(Keys.resolvers += "twttr" at "http://maven.twttr.com/")

  val core = deps(akkaHttp, akkaHttpSpray, akkaStream, scalike, scalikeJsr, mysqlConnector, pdkdf2)
  val logging = deps(logBack, logBackCore)
  val testKit = deps(scalaTest, akkaTestKit)

  private def deps(modules: ModuleID*): Seq[Setting[_]] = Seq(libraryDependencies ++= modules)
}
