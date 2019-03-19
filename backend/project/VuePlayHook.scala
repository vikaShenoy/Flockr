import play.sbt.PlayRunHook
import sbt._
import scala.sys.process.Process


/**
  * This hook captures the startup of the play framework and invokes 'npm install'.
  * After play is loaded the hook then executes 'npm run serv'.
  */

object VuePlayHook {
  def apply(base: File): PlayRunHook = {

    object NpmRunServe extends PlayRunHook {

      

  

//      override def beforeStarted(): Unit = {
//        val frontend = base / "../frontend"
//        Process("npm install", frontend ).run
//      }

      override def afterStarted(): Unit = {
        val frontend = base / "../frontend"
        // Windows cmd prefixes
        if (System.getProperty("os.name").toLowerCase().contains("win")){
          Process("cmd /c npm run serve", frontend ).run
        } else {
          Process("npm run serve", frontend ).run
        }
        
      }
    }

    NpmRunServe
  }
}