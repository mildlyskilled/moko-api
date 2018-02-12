import sbt._
import sbt.Keys._

object TheBuild extends Build {

  // -------------------------------------------------------------------------------------------------------------------
  // Root Project
  // -------------------------------------------------------------------------------------------------------------------

  lazy val root = Project("moko-root", file("."))
    .aggregate(testkit, core, moko)
    .configs(Configs.all: _*)
    .settings(Settings.root: _*)

  // -------------------------------------------------------------------------------------------------------------------
  // Modules
  // -------------------------------------------------------------------------------------------------------------------

  lazy val testkit: Project = Project("moko-testkit", file("moko-testkit"))
    .settings(unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (LocalProject("moko-core"), Compile))) // to avoid cyclic reference
    .configs(Configs.all: _*)
    .settings(Settings.testkit: _*)

  lazy val core: Project = Project("moko-core", file("moko-core"))
    .dependsOn(testkit % "test,integration")
    .configs(Configs.all: _*)
    .settings(Settings.core: _*)

  lazy val moko = Project("moko", file("moko"))
    .dependsOn(core, testkit % "test,integration")
    .configs(Configs.all: _*)
    .settings(Settings.moko: _*)
}