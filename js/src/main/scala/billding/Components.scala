package billding

import billding.ExerciseSessionComponent.{Firing, Relaxed, TriggerState}
import com.billding.exercises.{
  Exercise,
  ExerciseGenericWithReps,
  ExerciseHistory,
  PersistentDailyTotal
}
import com.raquo.airstream.eventbus.{EventBus, WriteBus}
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.Signal
import com.raquo.domtypes.jsdom.defs.events.TypedTargetMouseEvent
import com.raquo.laminar.api.L._
import com.raquo.laminar.modifiers.EventPropBinder
import com.raquo.laminar.nodes.ReactiveElement.Base
import com.raquo.laminar.nodes.ReactiveHtmlElement
import exercises.DailyQuantizedExercise
import org.scalajs.dom
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
    $exerciseTotal: Signal[PersistentDailyTotal],
    exercise: Exercise
  ): ReactiveHtmlElement[html.Div] =
    div(
      cls <-- $exerciseTotal.map(
        exerciseTotal =>
          if (exerciseTotal.count >= exercise.dailyGoal) "has-background-success" else ""
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
    exerciseHistory: ExerciseHistory
  ): ReactiveHtmlElement[html.Div] =
    div(
      cls := "is-size-5",
      exerciseHistory.exercises
        .map(
          exercise => div(span(exercise.day + ": "), span(exercise.count.toString))
        )
    )

  def FullyLoadedHistory(exercise: Exercise,
                         storage: Storage,
                         apiClient: ApiClient): ReactiveHtmlElement[html.Div] =
    div(
      child <-- EventStream
        .fromFuture(apiClient.getHistory(storage, exercise))
        .map(ExerciseHistory)
    )

  def ResetButton(
    x: EventPropBinder[TypedTargetMouseEvent[dom.Element]]
  ): ReactiveHtmlElement[html.Div] =
    div(
      button("Reset", cls := "button is-warning is-rounded is-size-4 my-1", x)
    )

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
    $exerciseTotal: Signal[PersistentDailyTotal]
  ): ReactiveHtmlElement[html.Div] = {
    /* This prevents a string conversion bug where I was displaying:
        PersistentDailyTotal(0)/20
       instead of:
        0/20
     */
    def renderFraction(numerator: Int, denominator: Int) =
      s"$numerator/$denominator"

    def indicateSelectedButton(
      ): Binder[HtmlElement] =
      cls <--
      $selectedComponent.combineWith($exerciseTotal).map {
        case (selectedExercise, dailyTotal) =>
          "small " +
          (if (selectedExercise == exercise)
             "is-primary has-background-primary"
           else {
             if (dailyTotal.count >= exercise.dailyGoal)
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
          exerciseTotal => renderFraction(exerciseTotal.count, exercise.dailyGoal)
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
          exerciseTotal =>
            (exerciseTotal.count <= 0) // TODO should this comparison be part of the total?
        ),
        onClick
          .map(_ => -exercise.repsPerSet)
          .map(Increment) --> exerciseCounter.submissionsWriter,
        s"-${exercise.repsPerSet}"
      ),
      button(
        cls := "button is-link is-rounded is-size-3 mx-2 my-2",
        onClick.map(_ => exercise.repsPerSet).map(Increment) --> exerciseCounter.submissionsWriter,
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

  def SpiralingStatusBars($percentageComplete: Signal[Int],
                          $exerciseTotal: Signal[PersistentDailyTotal],
                          exercise: ExerciseGenericWithReps) =
    HolyGrail(
      ReverseProgressBar($percentageComplete),
      DescendingVerticalProgressBar($percentageComplete),
      CounterDisplay($exerciseTotal, exercise),
      AscendingVerticalProgressBar($percentageComplete),
      ProgressBar($percentageComplete)
    )

}

class ServerBackedExerciseCounter(
  exercise: Exercise,
  postFunc: (Increment, Exercise) => Future[PersistentDailyTotal]
) {
  private val exerciseSubmissions = new EventBus[Increment]
  val submissionsWriter: WriteBus[Increment] = exerciseSubmissions.writer

  private val exerciseServerResultsBus = new EventBus[PersistentDailyTotal]

  val exerciseServerResultsBusEvents: EventStream[PersistentDailyTotal] =
    exerciseServerResultsBus.events

  private val exerciseServerResults: EventStream[PersistentDailyTotal] =
    exerciseSubmissions.events
      .map(submission => EventStream.fromFuture(postFunc(submission, exercise)))
      .flatten

  val $exerciseTotal: Signal[PersistentDailyTotal] =
    // The initial value is _not_ persistent, so I'm starting with a lie.
    exerciseServerResultsBus.events.foldLeft(PersistentDailyTotal(0))((acc, next) => next)

  private def percentageComplete(current: Int, goal: Int) =
    ((current.toFloat / goal.toFloat) * 100).toInt

  val $percentageComplete: Signal[Int] =
    $exerciseTotal.map(exerciseTotal => percentageComplete(exerciseTotal.count, exercise.dailyGoal))

  val $complete: Signal[Boolean] =
    $exerciseTotal.map(exercisetotal => exercisetotal.count >= exercise.dailyGoal)

  private val weirdExerciseCounterCycle
    : Binder[Base] = exerciseServerResults --> exerciseServerResultsBus

  private val initializeCount =
    EventStream
      .fromFuture(postFunc(Increment(0), exercise)) --> exerciseServerResultsBus

  val behavior: ReactiveHtmlElement[html.Div] =
    div(
      weirdExerciseCounterCycle,
      initializeCount
    )

}
