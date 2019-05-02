name := "SENG302 TEAM 500"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.8"

import sbt._
//import scala.sys.process._

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)


libraryDependencies += "com.google.inject" % "guice" % "4.2.2"
libraryDependencies += jdbc
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-core" % "2.3.0.1"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"
libraryDependencies += "io.cucumber" % "cucumber-java" % "4.2.0"
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "io.cucumber" % "cucumber-core" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-jvm" % "4.2.0" % Test
libraryDependencies += "io.cucumber" % "cucumber-junit" % "4.2.0" % Test
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")

configs(IntegrationTest)
Defaults.itSettings

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
scalacOptions := Seq("-target:jvm-1.8")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

PlayKeys.playRunHooks += VuePlayHook(baseDirectory.value)