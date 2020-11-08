package billding

import com.billding.exercises.{Exercise, ExerciseGenericWithReps}
import com.raquo.airstream.eventbus.{EventBus, WriteBus}
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.Signal
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement.Base
import com.raquo.laminar.nodes.ReactiveHtmlElement
import exercises.DailyQuantizedExercise
import org.scalajs.dom.raw.Storage
import org.scalajs.dom.{html, Storage}

import scala.concurrent.Future

object Components {

  def GoalExplanation(exercise: ExerciseGenericWithReps): ReactiveHtmlElement[html.Div] =
    div(
      cls("rep-explanation"),
      div(
        s"""Session: ${exercise.setsPerSession} set${if (exercise.setsPerSession > 1) "s"
        else ""} of ${exercise.repsPerSet}"""
      ),
      div(s"Daily Goal: ${exercise.dailyGoal} reps")
    )

  def CounterDisplay(
    $exerciseTotal: Signal[Int],
    exercise: Exercise
  ): ReactiveHtmlElement[html.Div] =
    div(
      cls <-- $exerciseTotal.map(
        currentCount => if (currentCount >= exercise.dailyGoal) "has-background-success" else ""
      ),
      div(exercise.humanFriendlyName, cls := "is-size-3"),
      child <-- $exerciseTotal.map(
        count => div(cls("has-text-centered is-size-1"), count.toString)
      )
    )

  // Notice that this is the only class so far that uses an org.scalajs class
  def BrokenLoginPrompt(storage: Storage): ReactiveHtmlElement[html.Div] =
    if (storage.getItem("access_token_fromJS") == "public")
      div(
        div("You're not actually logged in!"),
        a(href := "/oauth/login", cls := "button is-link is-rounded is-size-3", "Re-login")
      )
    else
      div()

  private def ExerciseHistory(
    quadsets: List[DailyQuantizedExercise]
  ): ReactiveHtmlElement[html.Div] =
    div(
      cls := "is-size-5",
      quadsets
        .map(
          quadSet => div(span(quadSet.day + ": "), span(quadSet.count.toString))
        )
    )

  def FullyLoadedHistory(exercise: Exercise,
                         storage: Storage): EventStream[ReactiveHtmlElement[html.Div]] =
    EventStream
      .fromFuture(ApiInteractions.getHistory(storage, exercise))
      .map(ExerciseHistory)

  def SelectorButton(
    $selectedComponent: Signal[Exercise],
    exercise: Exercise,
    componentSelections: WriteBus[Exercise],
    $exerciseTotal: Signal[Int]
  ): ReactiveHtmlElement[html.Div] = {
    def indicateSelectedButton(
      ): Binder[HtmlElement] =
      cls <--
      $selectedComponent.combineWith($exerciseTotal).map {
        case (selectedExercise, currentCount) =>
          "small " +
          (if (selectedExercise == exercise)
             "is-primary has-background-primary"
           else {
             if (currentCount >= exercise.dailyGoal)
               "is-success is-rounded is-light"
             else
               "is-link is-rounded "
           })
      }

    div(
      cls := "menu-item-with-count",
      div(
        cls := "has-text-left ml-1",
        exercise.humanFriendlyName
      ),
      div(
        child.text <-- $exerciseTotal.map(
          count => count + "/" + exercise.dailyGoal
        )
      ),
      indicateSelectedButton(),
      onClick.mapTo {
        exercise
      } --> componentSelections
    )

  }

}

class ServerBackedExerciseCounter(
  exercise: Exercise,
  postFunc: (Int, String) => Future[Int]
) {
  private val exerciseSubmissions = new EventBus[Int]
  val submissionsWriter: WriteBus[Int] = exerciseSubmissions.writer

  private val exerciseServerResultsBus = new EventBus[Int]
  val exerciseServerResultsBusEvents: EventStream[Int] = exerciseServerResultsBus.events

  private val exerciseServerResults: EventStream[Int] =
    exerciseSubmissions.events
      .map(submission => EventStream.fromFuture(postFunc(submission, exercise.id)))
      .flatten

  val $exerciseTotal: Signal[Int] =
    exerciseServerResultsBus.events.foldLeft(0)((acc, next) => next)

  private def percentageComplete(current: Int, goal: Int) =
    ((current.toFloat / goal.toFloat) * 100).toInt

  val $percentageComplete: Signal[Int] =
    $exerciseTotal.map(exerciseTotal => percentageComplete(exerciseTotal, exercise.dailyGoal))

  val $complete: Signal[Boolean] =
    $exerciseTotal.map(_ >= exercise.dailyGoal)

  private val weirdExerciseCounterCycle
    : Binder[Base] = exerciseServerResults --> exerciseServerResultsBus

  private val initializeCount =
    EventStream
      .fromFuture(postFunc(0, exercise.id)) --> exerciseServerResultsBus

  val behavior: ReactiveHtmlElement[html.Div] =
    div(
      weirdExerciseCounterCycle,
      initializeCount
    )

}
