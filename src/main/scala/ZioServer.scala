import java.util.concurrent.Executors

import zio.{DefaultRuntime, Runtime, Task, ZEnv, ZIO}
import zio.interop.catz.implicits._
import zio.interop.catz._
import fs2.Stream
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import repository.{Github, TodoRepository}
import service.{GithubService, TodoService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
//import scala.concurrent.ExecutionContext.global
import scala.util.Properties
import org.http4s.implicits._

object ZioServer extends CatsApp with Http4sDsl[Task]{
  implicit val ec = ExecutionContext .fromExecutor(Executors.newFixedThreadPool(10))
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
//    runtime.environment.
    for {
      client <- ZIO { BlazeClientBuilder[Task](global) }
//      githubService = new GithubService[Task](Github.impl[Task](client)).service
      githubService = new GithubService[Task](Github.impl[Task](???)).service
      httpApp = Router(
        "/github" -> githubService
      ).orNotFound
//      exitCode <- BlazeServerBuilder[Task]
//        .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
//        .withHttpApp(httpApp)
//        .serve
//          .compile[ZEnv, Nothing, Int]
//          .drain
    } yield {
      1
    }
    ???
  }

}
