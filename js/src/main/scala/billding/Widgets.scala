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
      cls := "has-background-success",
      percentageCompleted.toString + "%"
    )
  }

  def progressBar(percentageCompleted: Int) =
    div(
      width := s"$percentageCompleted%",
      cls := "has-background-success",
      percentageCompleted.toString + "%"
    )

  def reversedProgressBar(percentageCompleted: Int) =
    div(
      div(
        styleAttr := "float: left;",
        width := s"${100 - percentageCompleted}%",
        cls := "has-background-warning",
        "." // todo how do I make this appear without anything but whitespace?
      ),
      div(
        styleAttr := "float: right;",
        width := s"$percentageCompleted%",
        cls := "has-background-success is-justify-content-right",
        percentageCompleted.toString + "%"
      )
    )

}
