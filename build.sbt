//maintainer := "s302team500@cosc.canterbury.ac.nz"

import java.io.File
import scala.sys.process.Process

//javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
//scalacOptions += "-target:jvm-1.8"

val isWindows = System.getProperty("os.name").toLowerCase().contains("win")

// Executes a bash/cmd command
def runOnCommandline(script: String, dir: File): Int = {
  if(isWindows){ Process("cmd /c " + script, dir) } else { Process(script, dir) } }!

`run` := {
  val backendFolder = new File(baseDirectory.value, "backend");

  runOnCommandline("sbt run", backendFolder)
}