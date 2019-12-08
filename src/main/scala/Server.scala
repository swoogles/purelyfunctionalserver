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

object Server extends IOApp with Http4sDsl[IO] {
  val fallBackConfig = Config(
    ServerConfig("localhost",8080),
    DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")
  )
  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    for {
      configAttempt <- Stream.attemptEval(Config.load())
      config = configAttempt.getOrElse(fallBackConfig) // TODO Get from environment, *then* from config file
//      config <- Stream.eval(Config.load())
      transactor <- Stream.resource(Database.transactor(config.database))
      _ <- Stream.eval(Database.initialize(transactor))
      service = new TodoService(new TodoRepository(transactor)).service
      httpApp = Router(
        "/" -> service
//        , "/api" -> services
      ).orNotFound
      exitCode <- BlazeServerBuilder[IO]
        .bindHttp(config.server.port, config.server.host)
        .withHttpApp(httpApp)
        .serve
    } yield exitCode
  }

  override def run(args: List[String]): IO[ExitCode] =
    stream(args, IO {println("shutting down")}).compile.drain.as(ExitCode.Success)
}
