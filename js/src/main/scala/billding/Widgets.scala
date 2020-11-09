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
      div(
        styleAttr := "float: left;",
        width := s"$percentageCompleted%",
        cls := "has-background-success",
        percentageCompleted.toString + "%"
      ),
      div(
        styleAttr := "float: right;",
        width := s"${100 - percentageCompleted}%",
        cls := "has-background-warning",
        nbsp
      )
    )

  def reversedProgressBar(percentageCompleted: Int) =
    div(
      div(
        styleAttr := "float: left;",
        width := s"${100 - percentageCompleted}%",
        cls := "has-background-warning",
        nbsp
      ),
      div(
        styleAttr := "float: right;",
        width := s"$percentageCompleted%",
        cls := "has-background-success",
        nbsp
      )
    )

  def descendingVerticalProgressBar(percentageCompleted: Int) =
    div(
      height := "100%",
      width := "1em",
      div(
        height := s"$percentageCompleted%",
        cls := "has-background-success",
        nbsp
      ),
      div(
        height := s"${100 - percentageCompleted}%",
        cls := "has-background-warning",
        nbsp
      )
    )

  def ascendingVerticalProgressBar(percentageCompleted: Int) =
    div(
      height := "100%",
      width := "1em",
      div(
        height := s"${100 - percentageCompleted}%",
        cls := "has-background-warning",
        nbsp
      ),
      div(
        height := s"$percentageCompleted%",
        cls := "has-background-success",
        nbsp
      )
    )

}
