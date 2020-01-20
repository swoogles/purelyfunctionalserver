import java.util.concurrent.{Executors, ScheduledThreadPoolExecutor}

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import config.{Config, DatabaseConfig}
import db.Database
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import service._
import zio.{DefaultRuntime, Runtime, ZEnv}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Properties

object Server extends IOApp with Http4sDsl[IO] {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  val delayedExecutor = new ScheduledThreadPoolExecutor(1)
  implicit val runtime: Runtime[ZEnv] = new DefaultRuntime {}

  val fallBackConfig =
    DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")

  override def run(args: List[String]): IO[ExitCode] =
    stream(args, IO {
      println("shutting down")
    }).compile.drain.as(ExitCode.Success)

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    val configImpl = Config.impl()
    for {
      fullyBakedConfig <- Stream.eval(
        configImpl.configSteps()
      )
      transactor <- Stream.resource(Database.transactor(fullyBakedConfig.database)(ec))
      client <- BlazeClientBuilder[IO](global).stream
      _ <- Stream.eval(Database.initialize(transactor))
      blocker <- Stream.resource(Blocker[IO])
      httpApp = AllServices.initializeServicesAndRoutes[IO](transactor, client, blocker)

      _ <- Stream.eval(RepeatShit.infiniteIO(0).combine(RepeatShit.infiniteWeatherCheck))
      exitCode <- BlazeServerBuilder[IO]
        .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
    } yield exitCode
  }

}
