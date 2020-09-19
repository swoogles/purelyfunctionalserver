import java.util.concurrent.Executors

import cats.effect.{Clock, ConcurrentEffect, Timer}
import org.http4s.HttpRoutes

import scala.concurrent.duration.FiniteDuration

//import ZioServer.->
import zio.{DefaultRuntime, Runtime, Task, ZEnv, ZIO}
import zio.interop.catz.implicits._
import zio.interop.catz._
import fs2.Stream
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import repository.Github
import service.GithubService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
//import scala.concurrent.ExecutionContext.global
import scala.util.Properties
import org.http4s.implicits._
import zio.console._

/*
object ZioServer extends zio.App with Http4sDsl[ZIO]{
  implicit val ec = ExecutionContext .fromExecutor(Executors.newFixedThreadPool(10))
  /*
  implicit val ioTimer: Timer[ZIO] = new Timer[ZIO] {
    override def clock: Clock[ZIO] = zio.clock.Clock.Live.clock
    override def sleep(duration: FiniteDuration): ZIO[Unit] = ???
  }
 */
  implicit def f[ZIO]: ConcurrentEffect[ZIO] = ZIO[_]
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    for {
//      _ <- ZIO {}
      client <- Stream.resource {
        BlazeClientBuilder[ZIO](global).resource
      }
      //    runtime.environment.
      githubService = new GithubService[ZIO](Github.impl[ZIO](client)).service
      httpApp: HttpRoutes[ZIO] = Router(
        "/github" -> githubService
      ).orNotFound
      //      client <- ZIO { BlazeClientBuilder[Task](global) }
      //      githubService = new GithubService[Task](Github.impl[Task](client)).service
      builtServer = BlazeServerBuilder[ZIO](???, ???)
      .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
    server <-  Stream.eval(builtServer)
    } yield {
      server
          .code
//        .compile[Task, Task, ExitCode]
//        .drain
//        .fold(_ => 1, _ => 0)
    }


    ???
    }

}


 */
