import java.util.concurrent.{Executors, ScheduledThreadPoolExecutor}

import auth.OAuthLogic
import cats.data.Kleisli
import cats.effect.{Blocker, Clock, ConcurrentEffect, ExitCode, IO, IOApp}
import cats.implicits._
import config.{Config, ConfigData, DatabaseConfig}
import db.{Database, InMemoryAuthBackends}
import doobie.hikari.HikariTransactor
import fs2.Stream
import org.http4s.{Request, Response}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware._
import org.http4s.server.staticcontent.{FileService, fileService}
import pureconfig.error.ConfigReaderException
import repository._
import service._
import zio.{DefaultRuntime, Runtime, ZEnv}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
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
    for {
      fullyBakedConfig <- Stream.eval(
        configSteps()
      )
      clock: Clock[IO] = timer.clock
      transactor <- Stream.resource(Database.transactor(fullyBakedConfig.database)(ec))
      client <- BlazeClientBuilder[IO](global).stream
      _ <- Stream.eval(Database.initialize(transactor))
      blocker <- Stream.resource(Blocker[IO])
      httpApp = initializeServicesAndRoutes[IO](transactor, client, blocker)
      appWithMiddleWare = applyMiddleWareToHttpApp(httpApp)

      _ <- Stream.eval(RepeatShit.infiniteIO(0).combine(RepeatShit.infiniteWeatherCheck))
      exitCode <- BlazeServerBuilder[IO]
        .bindHttp(Properties.envOrElse("PORT", "8080").toInt, "0.0.0.0")
        .withHttpApp(appWithMiddleWare)
        .serve
    } yield exitCode
  }

  def configSteps() = {
    val configImpl =
      Config.impl[IO](
        (ex) => IO.raiseError[ConfigData](new ConfigReaderException[ConfigData](ex)),
        IO.pure,
        IO.pure,
        (ex) => IO.raiseError[DatabaseConfig](ex))

    configImpl.load().flatMap {
      configFromFile =>
        configImpl
          .loadDatabaseEnvironmentVariables()
          .map(envDbConfig => configFromFile.copy(database = envDbConfig))
          .orElse(IO.pure(configFromFile))
    }
  }

  def applyMiddleWareToHttpApp(httpApp: Kleisli[IO, Request[IO], Response[IO]]) = {
    val originConfig = CORSConfig(
      anyOrigin = false,
      allowedOrigins = Set("quadsets.netlify.com"),
      allowCredentials = false,
      maxAge = 1.day.toSeconds)
    val corsApp = CORS(httpApp, originConfig) // TODO Use this eventually
    httpApp
  }

  def initializeServicesAndRoutes[F[_]: ConcurrentEffect](
                                   transactor: HikariTransactor[IO],
                                   client: Client[IO],
                                   blocker: Blocker
                                 ): Kleisli[IO, Request[IO], Response[IO]] = {

    val todoService = new TodoService[IO](new TodoRepository[IO](transactor)).service

    val authLogic = new OAuthLogic[IO](client)
    val exerciseService =
      new ExerciseService[IO](
        new ExerciseLogic[IO](
          new ExerciseRepositoryImpl[IO](transactor)
        ),
        authLogic
      ).service

    val githubService = {
      new GithubService(Github.impl[IO](client)).service
    }
    val homePageService = new HomePageService[IO](blocker).routes
    val resourceService = fileService[IO](FileService.Config("./src/main/resources", blocker))
    val authService = new OAuthService[IO](client).service
    val authenticationBackends = new AuthenticationBackends(
      InMemoryAuthBackends.bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer,
      InMemoryAuthBackends.userStoreThatShouldBeInstantiatedOnceByTheServer,
    )
    val Auth = authenticationBackends.Auth

    val loginService =
      new LoginEndpoint[IO](
        authenticationBackends.userStore,
        authenticationBackends.bearerTokenStore,
      ).service

    val weatherService =
      Auth.liftService(
        new WeatherService[IO](WeatherApi.impl[IO](client)).service
      )

    val authenticatedEndpoint =
      Auth.liftService(
        new AuthenticatedEndpoint(
          authenticationBackends.bearerTokenStore
        ).service
      )

    Router(
      "/" -> homePageService,
      "/resources" -> resourceService,
      "/todo" -> todoService,
      "/github" -> githubService,
      "/exercises" -> exerciseService,
      "/weather" -> weatherService,
      "/oauth" -> authService,
      "/tsec" -> authenticatedEndpoint,
      "/login" -> loginService
    ).orNotFound
  }
}
