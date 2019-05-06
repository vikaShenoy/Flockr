import _root_.com.github.sbt.jacoco.JacocoPlugin.autoImport._

jacocoMergedDataFile := file("target/test-reports")

jacocoReportDirectory := file("target/test-reports")

jacocoReportSettings := JacocoReportSettings("SENG302 TEAM 500", None, JacocoThresholds(), Seq(JacocoReportFormats.XML, JacocoReportFormats.HTML), "utf-8")

jacocoExcludes := Seq("views*", "*Routes*", "controllers*routes*", "controllers*Reverse*", "controllers*javascript*", "controller*ref*")

lazy val root = (project in file(".")).configs(IntegrationTest)