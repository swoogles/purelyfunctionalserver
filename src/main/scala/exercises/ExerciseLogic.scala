package exercises

import fs2.Stream
import model.DailyQuantizedExercise
import zio.Task

class ExerciseLogic(exerciseRepository: ExerciseRepository) {

  def getExerciseHistoriesFor(name: String,
                              userIdOpt: String): Stream[Task, DailyQuantizedExercise] =
    exerciseRepository.getExerciseHistoryForUser(name, userIdOpt)

  def createOrUpdate(
    dailyQuantizedExercise: DailyQuantizedExercise
  ): Task[Either[IllegalStateException, DailyQuantizedExercise]] =
    exerciseRepository
      .deleteEmptyExerciseRecords(
        dailyQuantizedExercise.name,
        dailyQuantizedExercise.day,
        dailyQuantizedExercise.userId
      )
      .flatMap { deletedRows =>
        exerciseRepository
          .getExercise(dailyQuantizedExercise.name,
                       dailyQuantizedExercise.day,
                       dailyQuantizedExercise.userId)
          .flatMap {
            case Some(existingExercise) =>
              exerciseRepository
                .updateQuantizedExercise(
                  existingExercise,
                  dailyQuantizedExercise.count
                )
                .map {
                  case Right(updatedExercise) => Right(updatedExercise)
                  case Left(notFoundError) =>
                    Left(new IllegalStateException("Can't update a nonexistent exercise record."))
                }
            case None =>
              exerciseRepository
                .createExercise(dailyQuantizedExercise)
                .map(Right(_))
          }
      }

}
