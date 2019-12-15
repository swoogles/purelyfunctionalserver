package service

import cats.effect.Sync
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType}
import repository.Github

class GithubService[F[_]: Sync](repository: Github[F]) extends Http4sDsl[F] {

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "user" / user / "repoName" / repoName  =>
      Ok(
        Stream("[") ++
          Stream.eval(repository.get(user, repoName))
            .map(_.asJson.noSpaces) ++
          Stream("]")
        , `Content-Type`(MediaType.application.json))

    case GET -> Root / "user" / user  =>
      //      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))
      Ok(Stream("[") ++
        Stream.eval(repository.getUsersRecentActivity(user)).map(_.asJson.noSpaces) ++ Stream("]"), `Content-Type`(MediaType.application.json))

  }

}
