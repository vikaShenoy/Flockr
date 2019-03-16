name := "SENG302 TEAM 500"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.8"

import sbt._
import scala.sys.process._

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-core" % "2.3.0.1"
libraryDependencies += "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2"

libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")


javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")

PlayKeys.playRunHooks += VuePlayHook(baseDirectory.value)

