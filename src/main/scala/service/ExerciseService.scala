package service

import auth.{OAuthLogic, UserInfo}
import cats.effect.{Clock, ConcurrentEffect, IO}
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import model.DailyQuantizedExercise
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Request, Response, Uri}
import repository.ExerciseLogic

import scala.concurrent.duration.MILLISECONDS

class ExerciseService(
                                                  exerciseLogic: ExerciseLogic,
                                                  authLogic: OAuthLogic
                                             ) extends Http4sDsl[IO] {


      //  def authAuthChecks(pf: PartialFunction[AuthorizedRequest[F], F[Response[F]]])
      //    : PartialFunction[Request[F], F[Response[F]]] =

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // pf: PartialFunction[Request[F], F[Response[F]]]
        case request @ GET -> Root => {
          val user = authLogic.getUserFromRequest(request)
          println("Going to retrieve exercises for sub: " + user)
          Ok(
            Stream("[") ++
              exerciseLogic.getExerciseHistoriesFor("QuadSets", user.id)
                .map(_.asJson.noSpaces)
                .intersperse(",") ++ Stream("]")
            , `Content-Type`(MediaType.application.json)
          )
        }


        case req @ POST -> Root => {
          req.headers.foreach(header=>println("Header: " + header))
          println("Request: " + req)
          val user = authLogic.getUserFromRequest(req)
          req.params.foreach{case (key, value) => println("Exercise POST key: " + key + "   value: " + value)}
          for {
            newExercise <- req.decodeJson[DailyQuantizedExercise]
            completedExercise <- IO {
              println("Accepting a new POST from sub: " + user)
              newExercise.copy(userId = Some(user.id))
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

  }

}
