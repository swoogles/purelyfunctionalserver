name := "QuadSet Counter"

version := "0.2"

//scalaVersion := "2.13.1"
scalaVersion := "2.12.10"

lazy val CirceVersion = "0.12.3" // Options and java-8 modules are preventing me from further upgrades

enablePlugins(ScalaJSPlugin)

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
  "org.querki" %%% "jquery-facade" % "1.2"
)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

skip in packageJSDependencies := false
jsDependencies += // TODO Check for current version of JS
  "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js"
