ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "EnvironmentSpecificConfig",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.3",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    idePackagePrefix := Some("com.aluscent")
  )
