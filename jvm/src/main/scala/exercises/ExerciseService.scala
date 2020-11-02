package exercises

import auth.AuthLogic
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Type`, Location}
import org.http4s.{HttpRoutes, MediaType, Uri}
import zio.Task
import zio.interop.catz._

class ExerciseService(
  exerciseLogic: ExerciseLogic,
  authLogic: AuthLogic
) extends Http4sDsl[Task] {

  val service: HttpRoutes[Task] = HttpRoutes.of[Task] {
    // pf: PartialFunction[Request[F], F[Response[F]]]

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
      println("Authenticated user: " + user)
      for {
        newExercise <- req.decodeJson[DailyQuantizedExercise]
        wrappedResult <- exerciseLogic
          .createOrUpdate(newExercise.copy(userId = Some(user.id)))
          .map { successfullyCreatedExercise =>
            Created(
              successfullyCreatedExercise.count.toString,
              Location(Uri.unsafeFromString(s"/exercises/${successfullyCreatedExercise.id.get}"))
            )
          }
        bigResult <- wrappedResult
          .catchAll(
            error =>
              InternalServerError(
                "Unhandled error: " + error.getMessage
              )
          )
      } yield {
        bigResult
      }
    }

  }

}
