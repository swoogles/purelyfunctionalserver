import java.util.concurrent.{Executors, ScheduledThreadPoolExecutor}

import cats.effect.{ContextShift, ExitCode, IO, IOApp, Sync, Timer}
import zio.interop.catz._
import config.{Config, ConfigData, DatabaseConfig, ServerConfig}
import db.Database
import fs2.Stream
import cats.implicits._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.blaze.{BlazeBuilder, BlazeServerBuilder}
import pureconfig.error.ConfigReaderException
import repository.{ExerciseLogic, ExerciseRepository, ExerciseRepositoryImpl, Github, TodoRepository}
import service.{ExerciseService, GithubService, TodoService}
import zio.{DefaultRuntime, Runtime, ZEnv, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
//import scala.concurrent.ExecutionContext.global
import scala.util.Properties
import scala.concurrent.duration.{FiniteDuration, TimeUnit}
import cats.effect.{IO, Timer, Clock}
import scala.concurrent.duration._
import java.util.concurrent.ScheduledExecutorService
import org.http4s.server.middleware._

object Server extends IOApp with Http4sDsl[IO] {
  implicit val ec = ExecutionContext .fromExecutor(Executors.newFixedThreadPool(10))
  val delayedExecutor = new ScheduledThreadPoolExecutor(1)
  implicit val runtime: Runtime[ZEnv] = new DefaultRuntime {}


  val ecOne = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val ecTwo = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  val csOne: ContextShift[IO] = IO.contextShift(ecOne)
  val csTwo: ContextShift[IO] = IO.contextShift(ecTwo)

  val fallBackConfig =
    DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")

  def sleep(timespan: FiniteDuration): IO[Unit] =
    IO.cancelable { cb =>
      val tick = new Runnable {
        def run() = ec.execute(new Runnable {
          def run() = cb(Right(()))
        })
      }
      val f = delayedExecutor.schedule(tick, timespan.length, timespan.unit)
      IO(f.cancel(false))
    }
//  implicit val sync: Sync[IO] = IO[Unit]
//  , clock

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    for {
      configImpl <- Stream.eval(IO {
        Config.impl[IO](
          (ex) => IO.raiseError[ConfigData](new ConfigReaderException[ConfigData](ex)),
          IO.pure,
          IO.pure,
          (ex) => IO.raiseError[DatabaseConfig](ex))
      }
      )
      herokuDbConfigAttempt <- Stream.attemptEval(configImpl.loadDatabaseEnvironmentVariables())
      configAttempt <- Stream.attemptEval(configImpl.load())
      config = herokuDbConfigAttempt.getOrElse(configAttempt.map(_.database).getOrElse(fallBackConfig))
      clock: Clock[IO] = timer.clock
      transactor <- Stream.resource(Database.transactor(config)(ec))
      client <- BlazeClientBuilder[IO](global).stream
      _ <- Stream.eval(Database.initialize(transactor))
      service = new TodoService[IO](new TodoRepository[IO](transactor)).service
      exerciseService = {
        implicit val  implicitClock: Clock[IO] = clock

        (new ExerciseService[IO](
        new ExerciseLogic[IO](
          new ExerciseRepositoryImpl[IO](transactor)
        )
      )).service
      }
      githubService = {
//        new GithubService(Github.impl[ZIO](client)).service
        new GithubService(Github.impl[IO](client)).service
      }
      httpApp = Router(
        "/" -> service,
        "/github" -> githubService,
        "/exercises" -> exerciseService
      ).orNotFound
//      _ <- Stream.eval(sleep(5.seconds))
//      _ <- Stream.eval(IO.shift(ecOne))
//      _ <- Stream.eval(IO { println("hi")} *> IO.sleep(5.seconds) *> IO { println("delayed print ")})
//      _ <- Stream.eval(IO.shift(ecTwo))
      corsApp = CORS(httpApp) // allow quadsets.netlify.com
      exitCode <- BlazeServerBuilder[IO]
//        .bindHttp(config.server.port, config.server.host)
        .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
        .withHttpApp(corsApp)
        .serve
    } yield exitCode
  }

  override def run(args: List[String]): IO[ExitCode] =
    stream(args, IO {println("shutting down")}).compile.drain.as(ExitCode.Success)
}
