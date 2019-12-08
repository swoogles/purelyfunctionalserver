import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO}
import config.{Config, DatabaseConfig, ServerConfig}
import db.Database
import fs2.Stream
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.blaze.{BlazeBuilder, BlazeServerBuilder}
import repository.TodoRepository
import service.TodoService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import scala.util.Properties

object Server extends IOApp with Http4sDsl[IO] {
  implicit val ec = ExecutionContext .fromExecutor(Executors.newFixedThreadPool(10))

  val fallBackConfig =
    DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    for {
      herokuDbConfigAttempt <- Stream.attemptEval(Config.loadDatabaseEnvironmentVariables())
      configAttempt <- Stream.attemptEval(Config.load())
      config = herokuDbConfigAttempt.getOrElse(configAttempt.map(_.database).getOrElse(fallBackConfig))
//      config <- Stream.eval(Config.load())
      transactor <- Stream.resource(Database.transactor(config)(ec))
      _ <- Stream.eval(Database.initialize(transactor))
      service = new TodoService(new TodoRepository(transactor)).service
      httpApp = Router(
        "/" -> service
//        , "/api" -> services
      ).orNotFound
      exitCode <- BlazeServerBuilder[IO]
//        .bindHttp(config.server.port, config.server.host)
        .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
    } yield exitCode
  }

  override def run(args: List[String]): IO[ExitCode] =
    stream(args, IO {println("shutting down")}).compile.drain.as(ExitCode.Success)
}
