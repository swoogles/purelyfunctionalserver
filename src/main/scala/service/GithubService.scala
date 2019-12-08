package service

import cats.effect.IO
import model.{Importance, Todo, TodoNotFoundError}
import org.http4s.{HttpRoutes, HttpService, MediaType, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import repository.{Github, TodoRepository}
import io.circe.generic.auto._
import io.circe.syntax._
import fs2.Stream
import io.circe.{Decoder, Encoder}
import org.http4s.headers.{Location, `Content-Type`}

class GithubService(repository: Github[IO]) extends Http4sDsl[IO] {
  private implicit val encodeImportance: Encoder[Importance] = Encoder.encodeString.contramap[Importance](_.value)

  private implicit val decodeImportance: Decoder[Importance] = Decoder.decodeString.map[Importance](Importance.unsafeFromString)

  val service = HttpRoutes.of[IO] {
    case GET -> Root / "traffic" =>
      //      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))
      Ok(Stream("[") ++ Stream.eval(repository.get.map(_.asJson.noSpaces)) ++ Stream("]"), `Content-Type`(MediaType.application.json))

  }

  private def todoResult(result: Either[TodoNotFoundError.type, Todo]) = {
    result match {
      case Left(TodoNotFoundError) => NotFound()
      case Right(todo) => Ok(todo.asJson)
    }
  }
}
