package service

import cats.effect.{IO, Sync}
import org.http4s.{HttpRoutes, HttpService, MediaType, Uri}
import org.http4s.dsl.Http4sDsl
import repository.Github
import io.circe.syntax._
import fs2.Stream
import org.http4s.headers.{Location, `Content-Type`}
import io.circe.generic.auto._

class GithubService[F[_]: Sync](repository: Github[F]) extends Http4sDsl[F] {
  import Github.RepoActivity._

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "user" / user / "repoName" / repoName  =>
      //      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))
      Ok(Stream("[") ++ Stream.eval(repository.get(user, repoName)).map(_.asJson.noSpaces) ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "user" / user  =>
      //      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))
      Ok(Stream("[") ++
        Stream.eval(repository.getUsersRecentActivity(user)).map(_.asJson.noSpaces) ++ Stream("]"), `Content-Type`(MediaType.application.json))

  }

}
