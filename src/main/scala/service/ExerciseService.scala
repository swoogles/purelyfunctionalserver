package service

import java.time.ZoneId

import auth.{OAuthLogic, UserInfo}
import cats.effect.{Clock, ConcurrentEffect, IO, Sync}
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
                                                  exerciseLogic: ExerciseLogic[F],
                                                  authLogic: OAuthLogic[IO]
                                                )(implicit
F: Sync[F],
                                                  clock: Clock[F] // TODO Why can't this be F?
) extends Http4sDsl[IO] {

  val clocky: F[Long] = clock.monotonic(MILLISECONDS)
  val shittyJavaClock = java.time.Clock.systemDefaultZone()



  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root => {
      val accessToken = request.params.get("access_token")
      val userId =
      if (accessToken.isDefined) {
        val userInfo: UserInfo = authLogic.getUserInfo(accessToken.get).unsafeRunSync()
        Some(userInfo.sub)
      } else {
        None
      }
      println("Going to retrieve exercises for sub: " + userId)
      Ok(
        Stream("[") ++
          exerciseLogic.getExerciseHistoriesFor("QuadSets", userId)
            .map(_.asJson.noSpaces)
            .intersperse(",") ++ Stream("]")
        , `Content-Type`(MediaType.application.json)
      )
    }


      /*
    case GET -> Root => {
      Ok(
          Stream("[") ++
            exerciseLogic.getExercisesForToday( shittyJavaClock.instant().atZone(ZoneId.systemDefault()).toLocalDate)
            .map(_.asJson.noSpaces)
              .intersperse(",") ++ Stream("]")
          , `Content-Type`(MediaType.application.json)
        )

      }

       */



    case req @ POST -> Root => {
      req.headers.foreach(header=>println("Header: " + header))
      println("Request: " + req)
      req.params.foreach{case (key, value) => println("Exercise POST key: " + key + "   value: " + value)}
      val accessToken = req.params.get("access_token")
      if (accessToken.isDefined) {
        val userInfo: UserInfo = authLogic.getUserInfo(accessToken.get).unsafeRunSync()
        println("userInfo: " + userInfo)
      }
      for {
        newExercise <- req.decodeJson[DailyQuantizedExercise]
        completedExercise <- IO {
          if (accessToken.isDefined) {
            val userInfo: UserInfo = authLogic.getUserInfo(accessToken.get).unsafeRunSync()
            println("Accepting a new POST from sub: " + userInfo.sub)
            newExercise.copy(userId = Some(userInfo.sub))
          } else {
            newExercise
          }

        }
        _ <- IO {
          println("postedExercise day: " + newExercise.day)
        }
        wrappedResult <- exerciseLogic.createOrUpdate(completedExercise) map {
          case Right(successfullyCreatedExercise) =>
            Created(
              successfullyCreatedExercise.count.toString,
              Location(Uri.unsafeFromString(s"/exercises/${successfullyCreatedExercise.id.get}"))
            )
          case Left(illegalStateException) => {
            print("IllegalStateException wihle posting exercise: " + illegalStateException.getMessage)
            InternalServerError("We failed you.")
          }
        }
        bigResult <- wrappedResult
      }
        yield {
          bigResult
        }
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
