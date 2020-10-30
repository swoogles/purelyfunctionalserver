package exercises

import java.time.LocalDate

import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import model.ExerciseNotFoundError
import zio.Task
import zio.interop.catz._

trait ExerciseRepository {

  // TODO Push F throughout the rest of this file
  def getExercise(
                   exercise: DailyQuantizedExercise
                   ): Task[Option[DailyQuantizedExercise]]
  def deleteEmptyExerciseRecords(exercise: DailyQuantizedExercise): Task[Int]
  def getExerciseHistoryForUser(name: String, userId: String): Stream[Task, DailyQuantizedExercise]
  def createExercise(exercise: DailyQuantizedExercise): Task[DailyQuantizedExercise]

  def updateQuantizedExercise(
    exercise: DailyQuantizedExercise,
    reps: Int
  ): Task[Either[ExerciseNotFoundError.type, DailyQuantizedExercise]]
}

class ExerciseRepositoryImpl(transactor: Transactor[Task]) extends ExerciseRepository {
  //  private implicit val importanceMeta: Meta[Importance] = Meta[String].timap(Importance.unsafeFromString)( _.value)

  def getExerciseHistoryForUser(name: String,
                                userId: String): Stream[Task, DailyQuantizedExercise] =
    sql"""SELECT id, name, day, count, user_id FROM daily_quantized_exercises
          WHERE name = $name AND user_id = ${userId}
          ORDER BY day DESC
          LIMIT 14"""
      .query[DailyQuantizedExercise]
      .stream
      .transact(transactor)

  def getExercise(
                   exercise: DailyQuantizedExercise
                   ): Task[Option[DailyQuantizedExercise]] =
    sql"""SELECT id, name, day, count, user_id FROM daily_quantized_exercises
          WHERE name = ${exercise.name} AND day = ${exercise.day} AND user_id=${exercise.userId}"""
      .query[DailyQuantizedExercise]
      .option
      .transact(transactor)

  def deleteEmptyExerciseRecords(exercise: DailyQuantizedExercise): Task[Int] =
    sql"""DELETE FROM daily_quantized_exercises
          WHERE name = ${exercise.name} AND day = ${exercise.day} AND user_id=${exercise.userId} AND count = 0""".update.run
      .transact(transactor)

//  id: Long, name: String, day: LocalDate, count: Int
  def createExercise(exercise: DailyQuantizedExercise): Task[DailyQuantizedExercise] =
    sql"""
          INSERT INTO daily_quantized_exercises (
            name,
            day,
            count,
            user_id
          ) VALUES (
            ${exercise.name},
            ${exercise.day},
            ${exercise.count},
            ${exercise.userId}
            )""".stripMargin.update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map(id => exercise.copy(id = Some(id)))

  def updateQuantizedExercise(
    exercise: DailyQuantizedExercise,
    reps: Int
  ): Task[Either[ExerciseNotFoundError.type, DailyQuantizedExercise]] =
    sql"""UPDATE daily_quantized_exercises
          SET count = ${exercise.count + reps}
          WHERE day = ${exercise.day} AND name = ${exercise.name} AND user_id = ${exercise.userId}"""
      .update.run
      .transact(transactor)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(exercise.copy(count = exercise.count + reps))
        } else {
          Left(ExerciseNotFoundError)
        }
      }
}
