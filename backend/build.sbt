name := "SENG302 TEAM 500"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.8"

import sbt._
import scala.sys.process._
import java.io.File
import org.apache.commons.io.FileUtils
import java.nio.file.Files

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-core" % "2.3.0.1"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"
libraryDependencies += "io.cucumber" % "cucumber-java" % "4.2.0"
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-core" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-jvm" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-junit" % "4.2.0" % Test
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
libraryDependencies += evolutions


javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")

PlayKeys.playRunHooks += VuePlayHook(baseDirectory.value)

val isWindows = System.getProperty("os.name").toLowerCase().contains("win")

// Executes a bash/cmd command
def runOnCommandline(script: String, dir: File): Int = {
  if(isWindows){ Process("cmd /c " + script, dir) } else { Process(script, dir) } }!

// Installs, builds and copies frontend production files
def executeProdBuild(prodFrontendFolder: File, frontendFolder: File) = {
  runOnCommandline("npm install", frontendFolder);
  runOnCommandline("npm run build", frontendFolder);

  val distDir = new File(frontendFolder, "dist");
  FileUtils.deleteDirectory(prodFrontendFolder);
  FileUtils.copyDirectory(distDir, prodFrontendFolder);
}

val `build-frontend` = TaskKey[Unit]("run frontend build")

// Executes the task runner
`build-frontend` := {
  val publicFolder = new File(baseDirectory.value, "public");
  val prodFrontendFolder = new File(publicFolder, "vue-front-end-goes-here");
  val rootFolder = new File(baseDirectory.value, "..")
  val frontendFolder = new File(rootFolder, "frontend");

  executeProdBuild(prodFrontendFolder, frontendFolder);
}

// Specifies that frontend build task should run before dist
dist := (dist dependsOn `build-frontend`).value

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"



