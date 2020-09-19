package service

import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType}
import repository.Github
import zio.Task
import zio.interop.catz._

class GithubService(repository: Github) extends Http4sDsl[Task] {

  val service: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case GET -> Root / "user" / user / "repoName" / repoName =>
      Ok(Stream("[") ++
         Stream
           .eval(repository.get(user, repoName))
           .map(_.asJson.noSpaces) ++
         Stream("]"),
         `Content-Type`(MediaType.application.json))

    case GET -> Root / "user" / user =>
      Ok(Stream("[") ++
         Stream.eval(repository.getUsersRecentActivity(user)).map(_.asJson.noSpaces) ++ Stream("]"),
         `Content-Type`(MediaType.application.json))

  }

}
