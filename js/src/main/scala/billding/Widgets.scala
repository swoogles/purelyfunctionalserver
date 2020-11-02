package billding

import com.raquo.laminar.api.L._

object Widgets {
  private def percentageComplete(current: Int, goal: Int) =
    ((current.toFloat / goal.toFloat) * 100).toInt

  def progressBar(current: Int, goal: Int) = {
    // TODO A proper, pretty progress bar
    val percentageCompleted = percentageComplete(current, goal)
    div(
      width := s"$percentageCompleted%",
      backgroundColor := "green",
      percentageCompleted.toString
    )
  }


}
