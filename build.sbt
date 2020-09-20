lazy val commonSettings = Seq(
  organization := "com.billding",
  name := "purelyfunctionalserver",
  version := "0.0.1-SNAPSHOT",
//  scalaVersion := "2.13.0",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq(
    "-deprecation",
//    "-Xfatal-warnings", // TODO Re-enable once Circe is handled
    "-Ywarn-value-discard",
    "-Xlint:missing-interpolator"
  ),
)

lazy val Http4sVersion = "0.21.0-M3"

lazy val DoobieVersion = "0.8.6"

lazy val H2Version = "1.4.197"

lazy val FlywayVersion = "5.2.4"

//libraryDependencies += "io.circe" % "circe-java8_2.13.0-RC1" % "0.12.0-M1"
lazy val CirceVersion = "0.14.0-M1" // Options and java-8 modules are preventing me from further upgrades

lazy val PureConfigVersion = "0.12.1"

lazy val LogbackVersion = "1.2.3"

lazy val ScalaTestVersion = "3.2.0"

lazy val ScalaMockVersion = "4.4.0"

lazy val uTestVersion = "0.7.1"

lazy val zioVersion = "1.0.0-RC17"

lazy val tsecV = "0.2.0-M2"

testFrameworks += new TestFramework("utest.runner.Framework")

lazy val root = project.in(file(".")).
  aggregate(foo.js, foo.jvm).
  settings(
    publish := {},
    publishLocal := {},
    mainClass in (Compile) := Some("com.billding.Server")
  ).enablePlugins(JavaServerAppPackaging)

lazy val databaseExploration =
  crossProject(JSPlatform).in(file("databaseExploration"))
//  project.in(file("databaseExploration")).
//  .aggregate(foo.js)
  .settings(
    scalaVersion := "2.13.1",
    scalacOptions ++= Seq(
      "-deprecation",
      //    "-Xfatal-warnings", // TODO Re-enable once Circe is handled
      "-Ywarn-value-discard",
      "-Xlint:missing-interpolator"
    ),
    publish := {},
    publishLocal := {},
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % "0.10.3",   // Scala.js 1.x only
      "com.lihaoyi" %%% "fastparse" % "2.3.0"
    ),
    scalaJSUseMainModuleInitializer:=true
  )

lazy val foo =
  crossProject(JSPlatform, JVMPlatform).in(file("."))
  .configs(IntegrationTest)
  .settings(
    libraryDependencies ++= Seq(
    "io.circe" %%% "circe-generic" % CirceVersion,
    "io.circe" %%% "circe-literal" % CirceVersion,
    )
  )
  .jsSettings(
    name := "QuadSet Counter",
    version := "0.2",

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.1.0",
      "com.raquo" %%% "laminar" % "0.10.2",   // Scala.js 1.x only
      "com.softwaremill.sttp.client" %%% "core" % "2.2.7",

      "com.softwaremill.sttp.client" %%% "circe" % "2.2.7",
      //  "io.circe" %%% "circe-optics" % CirceVersion,
      //  "io.circe" %%% "circe-java8" % CirceVersion,
      "com.softwaremill.sttp.client" %%% "circe" % "2.0.0-RC5",
      "com.pauldijou" %% "jwt-core" % "4.2.0",
      "com.pauldijou" %% "jwt-circe" % "4.2.0",
      "com.lihaoyi" %%% "scalatags" % "0.9.1",
    )
  )
  .jvmSettings(
    mainClass in (Compile) := Some("com.billding.Server"),
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server"  % Http4sVersion,
      "org.http4s"            %% "http4s-circe"         % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"           % Http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"  % Http4sVersion,

      "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
      "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres"      % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,

      "com.h2database"        %  "h2"                   % H2Version,

      "org.flywaydb"          %  "flyway-core"          % FlywayVersion,

      "io.circe"              %% "circe-optics"         % "0.13.0"      % "it",
//      "io.circe"              %% "circe-java8"          % CirceVersion,
      "com.pauldijou" %% "jwt-core" % "4.2.0",
      "com.pauldijou" %% "jwt-circe" % "4.2.0",

      "com.github.pureconfig" %% "pureconfig"           % PureConfigVersion,

      "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,

      "org.scalatest"         %% "scalatest"            % ScalaTestVersion  % "it,test",
      "org.scalamock"         %% "scalamock"            % ScalaMockVersion  % "test",
      "com.lihaoyi"           %% "utest"                % uTestVersion % "test",
      "dev.zio"               %% "zio"                  % zioVersion,
      "dev.zio"               %% "zio-interop-cats"     % "2.0.0.0-RC10",
      "dev.zio"               %% "zio-macros-core"     % "0.5.0",
      "javax.servlet" % "javax.servlet-api" % "3.1.0", // No good for Scala
//      "com.nulab-inc" %% "scala-oauth2-core" % "1.5.0",
      "com.auth0" % "mvc-auth-commons" % "1.1.0",
      // Ditch all this tsec stuff if the Heroku/Java examples end up getting me there.
      "io.github.jmcardon" %% "tsec-common" % tsecV,
      "io.github.jmcardon" %% "tsec-password" % tsecV,
      "io.github.jmcardon" %% "tsec-cipher-jca" % tsecV,
      "io.github.jmcardon" %% "tsec-cipher-bouncy" % tsecV,
      "io.github.jmcardon" %% "tsec-mac" % tsecV,
      "io.github.jmcardon" %% "tsec-signatures" % tsecV,
      "io.github.jmcardon" %% "tsec-hash-jca" % tsecV,
      "io.github.jmcardon" %% "tsec-hash-bouncy" % tsecV,
//      "io.github.jmcardon" %% "tsec-libsodium" % tsecV,
      "io.github.jmcardon" %% "tsec-jwt-mac" % tsecV,
      "io.github.jmcardon" %% "tsec-jwt-sig" % tsecV,
      "io.github.jmcardon" %% "tsec-http4s" % tsecV,

)
).enablePlugins(JavaServerAppPackaging)

lazy val cbBuild = taskKey[Unit]("Execute the shell script")

cbBuild := {
  (foo.js/Compile/scalafmt).value
  (foo.js/Compile/fastOptJS).value
  (databaseExploration.js/Compile/fastOptJS).value
  (Compile/scalafmt).value
  import scala.sys.process._
  //  "ls ./target/scala-2.13" !
  (Process("mkdir ./jvm/src/main/resources/compiledJavaScript") #||
    Process("cp ./databaseExploration/js/target/scala-2.13/databaseexploration-fastopt.js ./jvm/src/main/resources/compiledJavaScript/") #&&
    Process("cp ./js/target/scala-2.13/quadset-counter-fastopt.js ./jvm/src/main/resources/compiledJavaScript/")
//    Process("cp ./target/scala-2.13/busriderapp-fastopt.js ./jvm/src/main/resources/compiledJavascript/") #&&
//    Process("cp ./target/scala-2.13/busriderapp-fastopt.js.map src/main/resources/compiledJavascript/") #&&
//    Process("cp sw/target/scala-2.13/sw-opt.js src/main/resources/") #&&
//    Process("cp sw/target/scala-2.13/sw-opt.js.map src/main/resources/") #&&
//    Process("cp ./target/scala-2.13/busriderapp-jsdeps.js src/main/resources/compiledJavascript/"))!
    )!
}
