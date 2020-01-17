name := "QuadSetTimer"

version := "0.1"

//scalaVersion := "2.13.1"
scalaVersion := "2.12.10"

lazy val CirceVersion = "0.12.3" // Options and java-8 modules are preventing me from further upgrades

enablePlugins(ScalaJSPlugin)

name := "Scala.js Tutorial"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.7"
libraryDependencies += "com.softwaremill.sttp.client" %%% "core" % "2.0.0-RC5"
libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client" %%% "circe" % "2.0.0-RC5",
  "io.circe" %%% "circe-generic" % CirceVersion,
  "io.circe" %%% "circe-literal" % CirceVersion,
//  "io.circe" %%% "circe-optics" % CirceVersion,
//  "io.circe" %%% "circe-java8" % CirceVersion,
  "com.softwaremill.sttp.client" %%% "circe" % "2.0.0-RC5",
  "com.pauldijou" %% "jwt-core" % "4.2.0",
  "com.pauldijou" %% "jwt-circe" % "4.2.0",
  "com.lihaoyi" %%% "scalatags" % "0.8.2",
)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true
