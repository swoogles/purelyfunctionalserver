import cats.effect.IO
import config.{Config, DatabaseConfig, ServerConfig}
import db.Database
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import repository.TodoRepository
import service.TodoService

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] with Http4sDsl[IO] {
  val fallBackConfig = Config(
    ServerConfig("localhost",8080),
    DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")
  )
  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    for {
      configAttempt <- Stream.attemptEval(Config.load())
      config = configAttempt.getOrElse(fallBackConfig) // TODO Get from environment, *then* from config file
//      config <- Stream.eval(Config.load())
      transactor <- Stream.eval(Database.transactor(config.database))
      _ <- Stream.eval(Database.initialize(transactor))
      exitCode <- BlazeBuilder[IO]
        .bindHttp(config.server.port, config.server.host)
        .mountService(new TodoService(new TodoRepository(transactor)).service, "/")
        .serve
    } yield exitCode
  }
}
