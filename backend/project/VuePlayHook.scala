import play.sbt.PlayRunHook
import sbt._
import scala.sys.process.Process

object VuePlayHook {
  def apply(base: File): PlayRunHook = {

    object NpmRunServe extends PlayRunHook {

      override def beforeStarted(): Unit = {
        val frontend = base / "../frontend"
        Process("npm install", frontend ).run
      }
      override def afterStarted(): Unit = {
        val frontend = base / "../frontend"
        Process("npm run serve", frontend ).run
      }
    }

    NpmRunServe
  }
}