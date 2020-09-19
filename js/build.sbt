name := "QuadSet Counter"

version := "0.2"

scalaVersion := "2.13.1"
//scalaVersion := "2.12.10"

lazy val CirceVersion = "0.13.0" // Options and java-8 modules are preventing me from further upgrades

enablePlugins(ScalaJSPlugin)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"
libraryDependencies ++= Seq(
  "com.raquo" %%% "laminar" % "0.10.2",   // Scala.js 1.x only
  "com.softwaremill.sttp.client" %%% "core" % "2.2.7",

"com.softwaremill.sttp.client" %%% "circe" % "2.2.7",
  "io.circe" %%% "circe-generic" % CirceVersion,
  "io.circe" %%% "circe-literal" % CirceVersion,
//  "io.circe" %%% "circe-optics" % CirceVersion,
//  "io.circe" %%% "circe-java8" % CirceVersion,
  "com.softwaremill.sttp.client" %%% "circe" % "2.0.0-RC5",
  "com.pauldijou" %% "jwt-core" % "4.2.0",
  "com.pauldijou" %% "jwt-circe" % "4.2.0",
  "com.lihaoyi" %%% "scalatags" % "0.9.1",
  "org.querki" %%% "jquery-facade" % "2.0"
)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

//skip in packageJSDependencies := false
//jsDependencies += //./External/todo-http4s-doobie/FrontEnd TODO Check for current version of JS
//  "org.webjars" % "jque./External/todo-http4s-doobie/FrontEndry" % "2.2.1" / "jquery.js" minified "jquery.min.js"
