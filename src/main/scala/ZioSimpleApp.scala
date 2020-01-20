/*
import Server.{ec}
import cats.Eval
import cats.effect.{Blocker, ExitCode, IO}
import cats.effect.internals.IOAppPlatform
import config.{ConfigData, DatabaseConfig, ServerConfig}
import db.Database
import fs2.Stream
import org.http4s.implicits._
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import repository.TodoRepository
import service.TodoService
import zio.clock.Clock
import zio.{IO, ManagedApp, RIO, Task, TaskR, ZIO, ZManaged}
import zio.console.{Console, putStrLn}
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.interop.catz._

object ZioSimpleApp extends zio.App {
//  implicit val env = environment.
  type AppEnvironment = Clock

  type AppTask[A] = RIO[AppEnvironment, A]

  //        .fold(_ => 1, _ => 0)
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    myAppLogic.fold(failure => {
      println("Failure: " + failure)
      println("ouch!")
      1
    }, _ => 0)
  }

  val z = ZEnv

//  val routes: HttpRoutes[ZIO] = ???



  /*
  val fullyBakedConfig  = ConfigData(ServerConfig("localhost", 8080), Server.fallBackConfig)
  for {
    transactor <- Database.transactor(fullyBakedConfig.database) (ec)
    val todoService = new TodoService[ZIO](new TodoRepository[ZIO](transactor)).service
       } yield ()

   */

  val server: ZIO[ZEnv, Throwable, Unit] = ZIO.runtime[ZEnv]
    .flatMap {
      implicit rts => {

        BlazeServerBuilder[Task]
          .bindHttp(8080, "localhost")
          .withHttpApp(???)
          .serve
          .compile
          .drain
      }
    }

  val myAppLogic: ZIO[Console, Exception, Int] =

//    BlazeServerBuilder[AppTask] //requires a Clock environment
//      .serve
//      .compile[AppTask, AppTask, ExitCode]
//      .drain

    for {
      _ <- putStrLn("Let's convert some scripts!")
      _ <- ZIO.fail(new Exception("We failed"))
//      _ = BlazeServerBuilder[Task]
      _ <- putStrLn("We converted scripts!!")
    } yield (1)

}

 */
