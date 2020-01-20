package service

import auth.OAuthLogic
import cats.data.Kleisli
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, IO}
import db.InMemoryAuthBackends
import doobie.hikari.HikariTransactor
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.staticcontent.{FileService, fileService}
import org.http4s.{Header, Request, Response}
import repository._

object AllServices {
  def initializeServicesAndRoutes[F[_]: ConcurrentEffect](
                                                           transactor: HikariTransactor[IO],
                                                           client: Client[IO],
                                                           blocker: Blocker
                                                         )(implicit cs: ContextShift[IO])
  : Kleisli[IO, Request[IO], Response[IO]] = {

    val todoService = new TodoService(new TodoRepository(transactor)).service

    val authLogic = new OAuthLogic(client)
    val exerciseService =
      new ExerciseService(
        new ExerciseLogic(
          new ExerciseRepositoryImpl(transactor)
        ),
        authLogic
      ).service


    val githubService = {
      new GithubService(Github.impl(client)).service
    }
    val homePageService = new HomePageService(blocker).routes
    val resourceService = fileService[IO](FileService.Config("./src/main/resources", blocker))
    val authService = new OAuthService(client, authLogic).service

    val myMiddle = new MyMiddle(authLogic)
    val authServiceWithExtraHeaders = myMiddle(authService, Header("SomeKey", "SomeValue"))
    val authenticationBackends = new AuthenticationBackends(
      InMemoryAuthBackends.bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer,
      InMemoryAuthBackends.userStoreThatShouldBeInstantiatedOnceByTheServer,
    )
    val Auth = authenticationBackends.Auth

    val loginService =
      new LoginEndpoint(
        authenticationBackends.userStore,
        authenticationBackends.bearerTokenStore,
      ).service

    val weatherService =
      Auth.liftService(
        new WeatherService(WeatherApi.impl(client)).service
      )

    val authenticatedEndpoint =
      Auth.liftService(
        new AuthenticatedEndpoint(
          authenticationBackends.bearerTokenStore
        ).service
      )

    Router(
      "/" -> myMiddle(homePageService, Header("SomeKey", "SomeValue")),
      //      "/resources" -> myMiddle.applyBeforeLogic(resourceService),
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
