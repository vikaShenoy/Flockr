import _root_.com.github.sbt.jacoco.JacocoPlugin.autoImport._

jacocoReportDirectory := file("target/jacoco")

jacocoReportSettings := JacocoReportSettings("SENG302 TEAM 500", None, JacocoThresholds(), Seq(JacocoReportFormats.HTML), "utf-8")

jacocoExcludes := Seq("views*", "*Routes*", "controllers*routes*", "controllers*Reverse*", "controllers*javascript*", "controller*ref*")

lazy val root = (project in file(".")).configs(IntegrationTest)