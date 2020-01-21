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
import zio.{DefaultRuntime, Runtime, Task, ZEnv, ZIO}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Properties
import zio.interop.catz._
import zio.interop.catz.implicits._

object Server extends zio.App with Http4sDsl[Task] {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  val delayedExecutor = new ScheduledThreadPoolExecutor(1)
  implicit val runtime: Runtime[ZEnv] = new DefaultRuntime {}

  val fallBackConfig =
    DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")

//  override def run(args: List[String]): Task[ExitCode] =
//    streamZio(args, Task {
//      println("shutting down")
//    }).compile.drain.as(ExitCode.Success)

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val streamResult: ZIO[Any, Throwable, Int] = streamZio(args, Task {
      println("shutting down")
    }).compile.drain.as(ExitCode.Success)
      .map( exitCode => exitCode.code)

    val catchResult: ZIO[Any, Nothing, Int] = streamResult
      .catchAll(error => ZIO.succeed(0))
    catchResult
  }

  def streamZio(args: List[String], requestShutdown: Task[Unit]): Stream[Task, ExitCode] = {
    val configImpl = Config.impl()
    for {
      fullyBakedConfig <- Stream.eval(
        configImpl.configSteps()
      )
      transactor <- Stream.resource(Database.transactor(fullyBakedConfig.database)(ec))
      client <- BlazeClientBuilder[Task](global).stream
      _ <- Stream.eval(Database.initialize(transactor))
      blocker <- Stream.resource(Blocker[Task])
      httpApp = AllServices.initializeServicesAndRoutes[Task](transactor, client, blocker)

//      _ <- Stream.eval(RepeatShit.infiniteIO(0).combine(RepeatShit.infiniteWeatherCheck))
      exitCode <- BlazeServerBuilder[Task]
        .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
    } yield exitCode
  }

}
