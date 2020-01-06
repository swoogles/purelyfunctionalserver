package repository

import java.time.LocalDate

import cats.effect.{IO, Sync}
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import model.{DailyQuantizedExercise, ExerciseNotFoundError}

trait ExerciseRepository[F[_]] {
  // TODO Push F throughout the rest of this file
  def getExercise(name: String, day: LocalDate, userId: Option[String]): IO[Option[DailyQuantizedExercise]]
  def getExercisesFor(name: String): Stream[IO, DailyQuantizedExercise]
  def getExerciseHistoryForUser(name: String, userId: String): Stream[IO, DailyQuantizedExercise]
  def createExercise(exercise: DailyQuantizedExercise): IO[DailyQuantizedExercise]
  def updateQuantizedExercise(exercise: DailyQuantizedExercise, reps: Int): IO[Either[ExerciseNotFoundError.type, DailyQuantizedExercise]]
}

class ExerciseRepositoryImpl[F[_]: Sync](transactor: Transactor[IO]) extends ExerciseRepository[F] {
  //  private implicit val importanceMeta: Meta[Importance] = Meta[String].timap(Importance.unsafeFromString)( _.value)

  def getExercisesFor(name: String): Stream[IO, DailyQuantizedExercise] =
    sql"SELECT id, name, day, count, user_id FROM daily_quantized_exercises WHERE name = $name ORDER BY day DESC"
      .query[DailyQuantizedExercise]
      .stream
      .transact(transactor)

  def getExerciseHistoryForUser(name: String, userId: String): Stream[IO, DailyQuantizedExercise] =
    sql"SELECT id, name, day, count, user_id FROM daily_quantized_exercises WHERE name = $name AND user_id = ${userId} ORDER BY day DESC"
      .query[DailyQuantizedExercise]
      .stream
      .transact(transactor)

  def getExercise(name: String, day: LocalDate, userId: Option[String]): IO[Option[DailyQuantizedExercise]] = {
    sql"""SELECT id, name, day, count, user_id FROM daily_quantized_exercises WHERE name = $name AND day = $day AND user_id=${userId}"""
      .query[DailyQuantizedExercise]
      .option
      .transact(transactor)
  }

  /*
  def getTodo(id: Long): IO[Either[ExerciseNotFoundError.type, Todo]] = {
    sql"SELECT id, description, importance FROM todo WHERE id = $id"
      .query[Todo]
      .option
      .transact(transactor).map {
      case Some(todo) => Right(todo)
      case None => Left(ExerciseNotFoundError)
    }
  }

   */
//  id: Long, name: String, day: LocalDate, count: Int
  def createExercise(exercise: DailyQuantizedExercise): IO[DailyQuantizedExercise] = {
    sql"""
         |INSERT INTO daily_quantized_exercises (
         |  name,
         |  day,
         |  count,
         |  user_id
         |) VALUES (
         |  ${exercise.name},
         |  ${exercise.day},
         |  ${exercise.count},
         |  ${exercise.userId}
         |  )""".stripMargin
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor).map { id => exercise.copy(id = Some(id))
    }
  }

  /*
  def deleteTodo(id: Long): IO[Either[ExerciseNotFoundError.type, Unit]] = {
    sql"DELETE FROM todo WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(ExerciseNotFoundError)
      }
    }
  }
   */

  def updateQuantizedExercise(exercise: DailyQuantizedExercise, reps: Int): IO[Either[ExerciseNotFoundError.type, DailyQuantizedExercise]] = {
    sql"UPDATE daily_quantized_exercises SET count = ${exercise.count + reps} WHERE day = ${exercise.day} AND name = ${exercise.name} AND user_id = ${exercise.userId}"
      .update
      .run
      .transact(transactor)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(exercise.copy(count = exercise.count + reps))
        } else {
          Left(ExerciseNotFoundError)
        }
      }
  }
}
