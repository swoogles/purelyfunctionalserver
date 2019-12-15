import java.util.concurrent.{Executors, ScheduledThreadPoolExecutor}

import cats.effect.{ContextShift, ExitCode, Fiber, IOApp}
import cats.implicits._
import config.{Config, ConfigData, DatabaseConfig}
import db.Database
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.error.ConfigReaderException
import repository._
import service.{ExerciseService, GithubService, TodoService, WeatherService}
import zio.{DefaultRuntime, Runtime, ZEnv}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.{Clock, IO, Timer}
import org.http4s.server.middleware._

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Properties

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
      exerciseService =
      new ExerciseService[IO](
        new ExerciseLogic[IO](
          new ExerciseRepositoryImpl[IO](transactor)
        )
      ).service

      githubService = {
        new GithubService(Github.impl[IO](client)).service
      }
      weatherService = new WeatherService[IO](WeatherApi.impl[IO](client)).service
      httpApp = Router(
        "/" -> service,
        "/github" -> githubService,
        "/exercises" -> exerciseService,
        "/weather" -> weatherService,
      ).orNotFound
      originConfig = CORSConfig(
        anyOrigin = false,
        allowedOrigins = Set("quadsets.netlify.com"),
        allowCredentials = false,
        maxAge = 1.day.toSeconds)
      corsApp = CORS(httpApp, originConfig) // TODO Use this eventually

      _ <- Stream.eval(RepeatShit.infiniteIO(0))
      _ <- Stream.eval(RepeatShit.infiniteWeatherCheck)
        exitCode <- BlazeServerBuilder[IO]
        .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
    } yield exitCode
  }

  override def run(args: List[String]): IO[ExitCode] =
    stream(args, IO {println("shutting down")}).compile.drain.as(ExitCode.Success)
}
