package repository

import java.time.LocalDate

import cats.effect.{IO, Sync}
import fs2.Stream
import model.DailyQuantizedExercise

class ExerciseLogic[F[_] : Sync](exerciseRepository: ExerciseRepository[F]) {

  def getExercisesFor(name: String): Stream[IO, DailyQuantizedExercise] =
    exerciseRepository.getExercisesFor(name)

  def getExerciseHistoriesFor(name: String, userIdOpt: Option[String]): Stream[IO, DailyQuantizedExercise] =
    userIdOpt match {
      case Some(userId) =>exerciseRepository.getExerciseHistoryForUser(name, userId)
      case None => exerciseRepository.getExercisesFor(name)
    }


  def createOrUpdate(dailyQuantizedExercise: DailyQuantizedExercise): IO[Either[ IllegalStateException, DailyQuantizedExercise]] = {

      exerciseRepository.getExercise(dailyQuantizedExercise.name, dailyQuantizedExercise.day)
        .flatMap{

          case Some(existingExercise) =>
            exerciseRepository.updateQuantizedExercise(existingExercise, dailyQuantizedExercise.count)
              .map {
                case Right(updatedExercise) => Right(updatedExercise)
                case Left(notFoundError) => Left(new IllegalStateException("Can't update a nonexistent exercise record."))
              }

          case None =>
            exerciseRepository.createExercise(dailyQuantizedExercise)
            .map(Right(_))
        }

  }

}
