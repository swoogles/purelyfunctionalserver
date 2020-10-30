package exercises

import fs2.Stream
import zio.Task

class ExerciseLogic(exerciseRepository: ExerciseRepository) {

  def getExerciseHistoriesFor(name: String,
                              userIdOpt: String): Stream[Task, DailyQuantizedExercise] =
    exerciseRepository.getExerciseHistoryForUser(name, userIdOpt)

  def createOrUpdate(
    dailyQuantizedExercise: DailyQuantizedExercise
  ): Task[DailyQuantizedExercise] =
    exerciseRepository
      .deleteEmptyExerciseRecords(dailyQuantizedExercise)
      .flatMap { deletedRows =>
        exerciseRepository
          .getExercise(dailyQuantizedExercise)
          .flatMap {
            case Some(existingExercise) =>
              exerciseRepository
                .updateQuantizedExercise(
                  existingExercise,
                  dailyQuantizedExercise.count
                )
                .map {
                  updatedExercise => updatedExercise
                    .getOrElse(throw new IllegalStateException("Can't update a nonexistent exercise record.") )
                }
            case None =>
              exerciseRepository
                .createExercise(dailyQuantizedExercise)
          }
      }

}
