package service

import auth.{AuthenticationBackends, LoginEndpoint, OAuthLogic, OAuthService}
import cats.data.Kleisli
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, IO}
import daml.{DamlService, TemplateLogic, TemplateRepositoryImpl}
import db.InMemoryAuthBackends
import doobie.hikari.HikariTransactor
import exercises.{ExerciseLogic, ExerciseRepositoryImpl, ExerciseService}
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.staticcontent.{FileService, fileService}
import org.http4s.{Header, HttpRoutes, Request, Response}
import repository._
import weather.{WeatherApi, WeatherService}
import zio.{Runtime, Task}
import zio.interop.catz._
import zio.interop.catz.implicits._

object AllServices {

  def initializeServicesAndRoutes[F[_]: ConcurrentEffect](
    transactor: HikariTransactor[Task],
    client: Client[Task],
    blocker: Blocker
  )(implicit cs: ContextShift[Task],
    runtime: Runtime[Any]): Kleisli[Task, Request[Task], Response[Task]] = {
    implicit val cs: ContextShift[Task] = zio.interop.catz.zioContextShift

    val authLogic = new OAuthLogic(client)
    val exerciseService =
      new ExerciseService(
        new ExerciseLogic(
          new ExerciseRepositoryImpl(transactor)
        ),
        authLogic
      ).service

    val damlService =
      new DamlService(
        new TemplateLogic(
          new TemplateRepositoryImpl(transactor)
        ),
        authLogic
      ).service

    val githubService = {
      new GithubService(Github.impl(client)).service
    }
    val homePageService: HttpRoutes[Task] = new HomePageService(blocker).routes
    val resourceService: HttpRoutes[Task] =
      fileService[Task](FileService.Config("./jvm/src/main/resources", blocker))
    val authService: HttpRoutes[Task] = new OAuthService(client, authLogic).service

    val myMiddle = new MyMiddle(authLogic)
//    val authServiceWithExtraHeaders = myMiddle(authService, Header("SomeKey", "SomeValue"))
    val authenticationBackends = new AuthenticationBackends(
      InMemoryAuthBackends.bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer,
      InMemoryAuthBackends.userStoreThatShouldBeInstantiatedOnceByTheServer
    )
    val Auth = authenticationBackends.Auth

    val loginService: HttpRoutes[Task] =
      new LoginEndpoint(
        authenticationBackends.userStore,
        authenticationBackends.bearerTokenStore
      ).service

    val weatherService =
      Auth.liftService(
        new WeatherService(WeatherApi.impl(client)).service
      )

//    val authenticatedEndpoint =
//      Auth.liftService(
//        new AuthenticatedEndpoint(
//          authenticationBackends.bearerTokenStore
//        ).service
//      )

    Router[Task](
      "/" -> homePageService,
      //      "/resources" -> myMiddle.applyBeforeLogic(resourceService),
      "/resources" -> resourceService,
      "/github"    -> githubService,
      "/exercises" -> exerciseService,
      "/weather" -> weatherService,
      "/oauth"     -> authService,
      "/daml"     -> damlService,
//      "/tsec" -> authenticatedEndpoint,
      "/login" -> loginService
    ).orNotFound
  }

}
