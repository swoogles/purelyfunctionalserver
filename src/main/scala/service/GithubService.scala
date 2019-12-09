package service

import cats.effect.IO
import org.http4s.{HttpRoutes, HttpService, MediaType, Uri}
import org.http4s.dsl.Http4sDsl
import repository.Github
import io.circe.syntax._
import fs2.Stream
import org.http4s.headers.{Location, `Content-Type`}

class GithubService(repository: Github[IO]) extends Http4sDsl[IO] {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "traffic" =>
      //      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))
      Ok(Stream("[") ++ Stream.eval(repository.get.map(_.asJson.noSpaces)) ++ Stream("]"), `Content-Type`(MediaType.application.json))

  }

}
