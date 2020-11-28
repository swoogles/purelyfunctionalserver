package billding

import billding.ExerciseSessionComponent.{Firing, Relaxed, TriggerState}
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

  def ExerciseHeader(headerText: String): ReactiveHtmlElement[html.Div] =
    div(headerText, cls := "is-size-3")

  def CounterDisplay(
    $exerciseTotal: Signal[Int],
    exercise: Exercise
  ): ReactiveHtmlElement[html.Div] =
    div(
      cls <-- $exerciseTotal.map(
        currentCount => if (currentCount >= exercise.dailyGoal) "has-background-success" else ""
      ),
      ExerciseHeader(exercise.humanFriendlyName),
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
                         storage: Storage,
                         apiClient: ApiClient): EventStream[ReactiveHtmlElement[html.Div]] =
    EventStream
      .fromFuture(apiClient.getHistory(storage, exercise))
      .map(ExerciseHistory)

  def BlinkyBox($countT: Signal[Counter],
                $counterState: Signal[TriggerState]): ReactiveHtmlElement[html.Div] = {
    val $color: Signal[String] =
      $counterState.map {
        case Firing  => "red"
        case Relaxed => "green"
      }

    div(cls("is-size-1 has-text-centered"),
        child.text <-- $countT.map(_.value.toString),
        styleAttr <-- $color.map(color => s"background: $color"))
  }

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

  def ControlCounterButtons(
    exerciseCounter: ServerBackedExerciseCounter,
    exercise: ExerciseGenericWithReps
  ) =
    div(
      button(
        cls := "button is-link is-rounded is-size-3 mx-2 my-2",
        disabled <--
        exerciseCounter.$exerciseTotal.map(
          exerciseTotal => (exerciseTotal <= 0)
        ),
        onClick
          .map(_ => -exercise.repsPerSet) --> exerciseCounter.submissionsWriter,
        s"-${exercise.repsPerSet}"
      ),
      button(
        cls := "button is-link is-rounded is-size-3 mx-2 my-2",
        onClick.map(_ => exercise.repsPerSet) --> exerciseCounter.submissionsWriter,
        s"+${exercise.repsPerSet}"
      )
    )

  def Kudos($complete: Signal[Boolean]): Inserter[Base] =
    child.text <-- $complete.map {
      case true  => "Good job! You reached your daily goal!"
      case false => ""
    }

  def ProgressBar($percentageComplete: Signal[Int]): Inserter[Base] =
    child <-- $percentageComplete.map(
      Widgets.progressBar
    )

  def ReverseProgressBar($percentageComplete: Signal[Int]) =
    child <-- $percentageComplete.map(
      Widgets.reversedProgressBar
    )

  def DescendingVerticalProgressBar($percentageComplete: Signal[Int]) =
    child <-- $percentageComplete.map(
      Widgets.descendingVerticalProgressBar
    )

  def AscendingVerticalProgressBar($percentageComplete: Signal[Int]) =
    child <-- $percentageComplete.map(
      Widgets.ascendingVerticalProgressBar
    )

  def HolyGrail(
    headerContent: Inserter[Base],
    leftContent: Inserter[Base],
    mainContent: ReactiveHtmlElement[html.Div],
    rightContent: Inserter[Base],
    footerContent: Inserter[Base]
  ) =
    div(
      cls := "holy-grail",
      div(cls := "holy-header", headerContent),
      div(cls := "left-sidebar", leftContent),
      div(cls := "holy-main", mainContent),
      div(cls := "right-sidebar", rightContent),
      div(cls := "holy-footer", footerContent)
    )
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
