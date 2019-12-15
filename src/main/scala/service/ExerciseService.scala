package service

import java.time.ZoneId

import cats.effect.{Clock, ConcurrentEffect, IO}
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import model.DailyQuantizedExercise
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import repository.ExerciseLogic

import scala.concurrent.duration.MILLISECONDS

class ExerciseService[F[_]: ConcurrentEffect](
                                                  exerciseLogic: ExerciseLogic[F]
                                                )(implicit
                                                  clock: Clock[IO] // TODO Why can't this be F?
) extends Http4sDsl[IO] {

  val clocky: IO[Long] = clock.monotonic(MILLISECONDS)
  val shittyJavaClock = java.time.Clock.systemDefaultZone()

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => {
      /*
      Ok(
        for {
          res <- IO {"hi"}
        } yield {
          res
        }
      )

       */

//      /*
      Ok(
          Stream("[") ++
            exerciseLogic.getExercisesForToday( shittyJavaClock.instant().atZone(ZoneId.systemDefault()).toLocalDate)
            .map(_.asJson.noSpaces)
              .intersperse(",") ++ Stream("]")
          , `Content-Type`(MediaType.application.json)
        )

      }



      /*
    case GET -> Root =>
      for {
        getResult <- exerciseLogic.getTodo(id)
        response <- todoResult(getResult)
      } yield response

      */

    case req @ POST -> Root =>
      for {
        newExercise <- req.decodeJson[DailyQuantizedExercise]
        wrappedResult <- exerciseLogic.createOrUpdate(newExercise) map {
          case Right(successfullyCreatedExercise) =>
            Created(
              successfullyCreatedExercise.asJson,
              Location(Uri.unsafeFromString(s"/exercises/${successfullyCreatedExercise.id.get}"))
            )
          case Left(illegalStateException) => InternalServerError("We failed you.")
        }
        bigResult <- wrappedResult
      }
       yield {
        bigResult
      }

      /*
    case req @ PUT -> Root / "todos" / LongVar(id) =>
      for {
        todo <-req.decodeJson[Todo]
        updateResult <- exerciseLogic.updateTodo(id, todo)
        response <- todoResult(updateResult)
      } yield response

    case DELETE -> Root / "todos" / LongVar(id) =>
      exerciseLogic.deleteTodo(id).flatMap {
        case Left(TodoNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }

       */
  }

}
