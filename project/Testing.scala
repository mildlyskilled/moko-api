import sbt.{Def, _}
import sbt.Keys._

import scala.collection.immutable.Seq

object Testing {
  import Configs._
  import BuildKeys._

  private lazy val itSettings =
    inConfig(IntegrationTest)(Defaults.testSettings) ++
      Seq(
        fork in IntegrationTest := false,
        parallelExecution in IntegrationTest := false,
        scalaSource in IntegrationTest := baseDirectory.value / "src/integration/scala")

  lazy val settings: scala.Seq[Def.Setting[_]] = itSettings ++ Seq(
    testAll <<= (test in IntegrationTest).dependsOn(test in Test)
  )
}
