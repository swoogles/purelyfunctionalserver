package exercises

import java.time.LocalDate

case class DailyQuantizedExercise(id: Option[Long],
                                  name: String,
                                  day: LocalDate,
                                  count: Int,
                                  userId: Option[String])

object DailyQuantizedExercise {

  def apply(name: String, day: LocalDate, count: Int): DailyQuantizedExercise =
    DailyQuantizedExercise(None, name, day, count, None)

}
