package repository

import cats.effect.IO
import fs2.Stream
import model.DailyQuantizedExercise
import zio.Task

class ExerciseLogic(exerciseRepository: ExerciseRepository) {

  def getExercisesFor(name: String): Stream[Task, DailyQuantizedExercise] =
    exerciseRepository.getExercisesFor(name)

  def getExerciseHistoriesFor(name: String, userIdOpt: String): Stream[Task, DailyQuantizedExercise] =
    exerciseRepository.getExerciseHistoryForUser(name, userIdOpt)

  def createOrUpdate(dailyQuantizedExercise: DailyQuantizedExercise): Task[Either[ IllegalStateException, DailyQuantizedExercise]] = {

      exerciseRepository.getExercise(dailyQuantizedExercise.name, dailyQuantizedExercise.day, dailyQuantizedExercise.userId)
        .flatMap{

          case Some(existingExercise) =>
            println("updating existing exercises: " + existingExercise)
            exerciseRepository.updateQuantizedExercise(existingExercise, dailyQuantizedExercise.count)
              .map {
                case Right(updatedExercise) => Right(updatedExercise)
                case Left(notFoundError) => Left(new IllegalStateException("Can't update a nonexistent exercise record."))
              }

          case None =>
            println("Going to creat a new exercise")
            exerciseRepository.createExercise(dailyQuantizedExercise)
            .map(Right(_))
        }

  }

}
