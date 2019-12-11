package service

import cats.effect.{IO, Sync}
import model.{Importance, Todo, TodoNotFoundError}
import org.http4s.{HttpRoutes, HttpService, MediaType, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import repository.TodoRepository
import io.circe.generic.auto._
import io.circe.syntax._
import fs2.Stream
import io.circe.{Decoder, Encoder}
import org.http4s.headers.{Location, `Content-Type`}

class TodoService[F[_]: Sync](repository: TodoRepository[F]) extends Http4sDsl[IO] {
  private implicit val encodeImportance: Encoder[Importance] = Encoder.encodeString.contramap[Importance](_.value)

  private implicit val decodeImportance: Decoder[Importance] = Decoder.decodeString.map[Importance](Importance.unsafeFromString)

  val service = HttpRoutes.of[IO] {
    case GET -> Root / "todos" =>
//      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))
      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "todos" / LongVar(id) =>
      for {
        getResult <- repository.getTodo(id)
        response <- todoResult(getResult)
      } yield response

    case req @ POST -> Root / "todos" =>
      for {
        todo <- req.decodeJson[Todo]
        createdTodo <- repository.createTodo(todo)
        secondCreatedTodo <- repository.createTodo(
          createdTodo.copy(
            id=todo.id.map(_*1000),
            description=todo.description + "copied"
          )
        )
        response <- Created(createdTodo.asJson, Location(Uri.unsafeFromString(s"/todos/${createdTodo.id.get}")))
      } yield response

    case req @ PUT -> Root / "todos" / LongVar(id) =>
      for {
        todo <-req.decodeJson[Todo]
        updateResult <- repository.updateTodo(id, todo)
        response <- todoResult(updateResult)
      } yield response

    case DELETE -> Root / "todos" / LongVar(id) =>
      repository.deleteTodo(id).flatMap {
        case Left(TodoNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }
  }

  private def todoResult(result: Either[TodoNotFoundError.type, Todo]) = {
    result match {
      case Left(TodoNotFoundError) => NotFound()
      case Right(todo) => Ok(todo.asJson)
    }
  }
}
