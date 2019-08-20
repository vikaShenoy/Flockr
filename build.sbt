name := "SENG302 TEAM 500"

version := "0.0.1-SNAPSHOT"

maintainer := "s302team500@cosc.canterbury.ac.nz"

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
libraryDependencies += "io.cucumber" % "cucumber-core" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-jvm" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-junit" % "4.2.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.23" % Test
libraryDependencies += "com.novocode" % "junit-interface" % "0.8" % "test->default"
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
libraryDependencies += evolutions
libraryDependencies ++= Seq(
  javaWs
)

// Jacoco Settings
configs(IntegrationTest)
Defaults.itSettings
javacOptions := Seq("-source", "1.8", "-target", "1.8", "-Xlint")
scalacOptions := Seq("-target:jvm-1.8")
//initialize := {
//  val _ = initialize.value
//  if (sys.props("java.specification.version") != "1.8")
//    sys.error("Java 8 is required for this project.")
//}

//// Checkstyle Settings
//checkstyleConfigLocation := CheckstyleConfigLocation.File("./conf/checkstyle-config.xml")
//(checkstyle in Test) := (checkstyle in Test).triggeredBy(compile in Test).value

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
  val frontendFolder = new File(baseDirectory.value, "frontend");

  executeProdBuild(prodFrontendFolder, frontendFolder);
}

// Specifies that frontend build task should run before dist
dist := (dist dependsOn `build-frontend`).value

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))



