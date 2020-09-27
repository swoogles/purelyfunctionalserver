package exercises

import auth.AuthLogic
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import zio.Task
import zio.interop.catz._

class ExerciseService(
  exerciseLogic: ExerciseLogic,
  authLogic: AuthLogic
) extends Http4sDsl[Task] {

  val service: HttpRoutes[Task] = HttpRoutes.of[Task] {
    // pf: PartialFunction[Request[F], F[Response[F]]]
    case request @ GET -> Root => {
      val user = authLogic.getUserFromRequest(request)
      Ok(
        Stream("[") ++
        exerciseLogic
          .getExerciseHistoriesFor("QuadSets", user.id)
          .map(_.asJson.noSpaces)
          .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )
    }

    case request @ GET -> Root / exerciseName => {
      val user = authLogic.getUserFromRequest(request)
      Ok(
        Stream("[") ++
          exerciseLogic
            .getExerciseHistoriesFor(exerciseName, user.id)
            .map(_.asJson.noSpaces)
            .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )
    }

    case req @ POST -> Root => {
      val user = authLogic.getUserFromRequest(req)
      for {
        newExercise       <- req.decodeJson[DailyQuantizedExercise]
        completedExercise <- Task { newExercise.copy(userId = Some(user.id)) } // TODO Does not need to be a task.
        wrappedResult <- exerciseLogic.createOrUpdate(completedExercise).map {
          case Right(successfullyCreatedExercise) =>
            Created(
              successfullyCreatedExercise.count.toString,
              Location(Uri.unsafeFromString(s"/exercises/${successfullyCreatedExercise.id.get}"))
            )
          case Left(illegalStateException) =>
            InternalServerError("IllegalStateException while posting exercise: " + illegalStateException.getMessage)
        }
        bigResult <- wrappedResult
      } yield {
        bigResult
      }
    }

  }

}
