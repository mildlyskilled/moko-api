import sbt._

object Configs {
  val IntegrationTest = config("integration") extend (Runtime)
  val all = Seq(IntegrationTest)
}
