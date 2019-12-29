lazy val commonSettings = Seq(
  organization := "com.billding",
  name := "purelyfunctionalserver",
  version := "0.0.1-SNAPSHOT",
//  scalaVersion := "2.13.0",
  scalaVersion := "2.12.8",
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

lazy val CirceVersion = "0.11.0" // Options and java-8 modules are preventing me from further upgrades

lazy val PureConfigVersion = "0.12.1"

lazy val LogbackVersion = "1.2.3"

lazy val ScalaTestVersion = "3.0.5"

lazy val ScalaMockVersion = "4.1.0"

lazy val uTestVersion = "0.7.1"

lazy val zioVersion = "1.0.0-RC17"

lazy val tsecV = "0.2.0-M2"

testFrameworks += new TestFramework("utest.runner.Framework")

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
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

      "io.circe"              %% "circe-generic"        % CirceVersion,
      "io.circe"              %% "circe-literal"        % CirceVersion      % "it,test",
      "io.circe"              %% "circe-optics"         % CirceVersion      % "it",
      "io.circe"              %% "circe-java8"          % CirceVersion,
      "com.pauldijou" %% "jwt-core" % "4.2.0",
      "com.pauldijou" %% "jwt-circe" % "4.2.0",

      "com.github.pureconfig" %% "pureconfig"           % PureConfigVersion,

      "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,

      "org.scalatest"         %% "scalatest"            % ScalaTestVersion  % "it,test",
      "org.scalamock"         %% "scalamock"            % ScalaMockVersion  % "test",
      "com.lihaoyi"           %% "utest"                % uTestVersion % "test",
      "dev.zio"               %% "zio"                  % zioVersion,
      "dev.zio"               %% "zio-interop-cats"     % "2.0.0.0-RC10",
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
