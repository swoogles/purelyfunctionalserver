package billding

import com.billding.exercises.{Exercise, ExerciseGenericWithReps}
import com.billding.{FULL, SoundStatus}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.signal.Signal
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html
import org.scalajs.dom.raw.Storage

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

// Stuff in this block might belong in a separate location
sealed trait CounterAction
case object ResetCount extends CounterAction
case class Increment(value: Int) extends CounterAction
case object DoNotUpdate extends CounterAction

case class Counter(value: Int)

object CounterAction {

  def update(counterAction: CounterAction, counter: Counter) =
    counterAction match {
      case ResetCount           => Counter(0)
      case increment: Increment => counter.copy(counter.value + increment.value)
      case DoNotUpdate          => counter
    }
}
// /Stuff

trait ExerciseSessionComponent {
  val exercise: Exercise
  val exerciseSelectButton: ReactiveHtmlElement[html.Div]
  val exerciseSessionComponent: ReactiveHtmlElement[html.Div]
}

case class RepeatingElement() extends RepeatWithIntervalHelper

object ExerciseSessionComponent {

  def ManualExerciseComponent(
    exercise: ExerciseGenericWithReps,
    $selectedComponent: Signal[Exercise],
    storage: Storage,
    updateMonitor: Observer[Boolean],
    soundCreator: SoundCreator,
    exerciseCounter: ServerBackedExerciseCounter,
    soundStatus: Signal[SoundStatus]
  ): ReactiveHtmlElement[html.Div] = {

    val counterAndSoundStatusObserver = Observer[(Int, SoundStatus, Exercise)] {
      case (currentCount, soundStatus, selectedExercise) =>
        if (soundStatus == FULL && selectedExercise == exercise) {
          if (currentCount == exercise.dailyGoal) soundCreator.goalReached.play()
          else soundCreator.addExerciseSet.play()
        }
    }

    div(
      exerciseCounter.behavior,
      exerciseCounter.$exerciseTotal.changes
        .withCurrentValueOf(soundStatus)
        .withCurrentValueOf($selectedComponent)
        .map {
          case ((counter, soundStatus), selectedComponent) =>
            (counter, soundStatus, selectedComponent)
        } --> counterAndSoundStatusObserver,
      exerciseCounter.$complete --> updateMonitor,
      conditionallyDisplay(exercise, $selectedComponent),
      Components.BrokenLoginPrompt(storage),
      Components.CounterDisplay(exerciseCounter.$exerciseTotal, exercise),
      Components.ProgressBar(exerciseCounter.$percentageComplete),
      Components.ControlCounterButtons(exerciseCounter, exercise),
      Components.GoalExplanation(exercise),
      Components.Kudos(exerciseCounter.$complete)
    )
  }

  class ExerciseSessionComponentWithExternalStatus(
    componentSelections: WriteBus[Exercise],
    val exercise: ExerciseGenericWithReps,
    $selectedComponent: Signal[Exercise],
    postFunc: (Int, String) => Future[Int],
    soundCreator: SoundCreator,
    storage: Storage,
    updateMonitor: Observer[Boolean],
    soundStatus: Signal[SoundStatus]
  ) extends ExerciseSessionComponent {

    val exerciseCounter =
      new ServerBackedExerciseCounter(exercise, postFunc)

    val exerciseSelectButton: ReactiveHtmlElement[html.Div] =
      Components.SelectorButton(
        $selectedComponent,
        exercise: Exercise,
        componentSelections,
        exerciseCounter.$exerciseTotal
      )

    val exerciseSessionComponent: ReactiveHtmlElement[html.Div] =
      ManualExerciseComponent(
        exercise,
        $selectedComponent,
        storage,
        updateMonitor,
        soundCreator,
        exerciseCounter,
        soundStatus
      )

  }

  def conditionallyDisplay(
    id: Exercise,
    $selectedComponent: Signal[Exercise]
  ): Binder[HtmlElement] = {
    def createCssContent =
      (selection: Exercise) => s"""display: ${if (selection == id) "inline" else "none"}"""

    styleAttr <-- $selectedComponent.map(createCssContent)
  }

  sealed trait TriggerState

  case object Firing extends TriggerState

  case object Relaxed extends TriggerState

  case class TickingExerciseCounterComponent(componentSelections: WriteBus[Exercise],
                                             exercise: Exercise,
                                             $selectedComponent: Signal[Exercise],
                                             postFunc: (Int, String) => Future[Int],
                                             storage: Storage,
                                             soundCreator: SoundCreator,
                                             $soundStatus: Signal[SoundStatus])
      extends ExerciseSessionComponent {

    private val exerciseCounter =
      new ServerBackedExerciseCounter(exercise, postFunc)

    private val clockTicks = new EventBus[Int]

    private val $triggerState: Signal[TriggerState] =
      clockTicks.events.foldLeft[TriggerState](Relaxed)(
        (counterState, _) => if (counterState == Relaxed) Firing else Relaxed
      )

    private val counterActionBus = new EventBus[CounterAction]()

    // Doing this to get the initial count
    private val $countT: Signal[Counter] =
      counterActionBus.events.withCurrentValueOf($selectedComponent).foldLeft(Counter(0)) {
        case (acc, (next, selectedComponent)) =>
          if (selectedComponent == exercise)
            CounterAction.update(next, acc)
          else
            acc
      }

    private val $countVar: Var[Counter] = Var(Counter(0))

    // TODO See if this triggers unwanted noises when soundStatus changes
    val counterAndSoundStatusObserver = Observer[(TriggerState, SoundStatus, Exercise)] {
      case (triggerState, soundStatus, selectedExercise) => {
        if (soundStatus == FULL && selectedExercise == exercise) {
          if (triggerState == Firing) soundCreator.startSound.play()
          else soundCreator.endSound.play()
        }
      }
    }

    val exerciseSelectButton: ReactiveHtmlElement[html.Div] =
      Components.SelectorButton(
        $selectedComponent,
        exercise: Exercise,
        componentSelections,
        exerciseCounter.$exerciseTotal
      )

    val duration = new FiniteDuration(10, scala.concurrent.duration.SECONDS)

    val exerciseSessionComponent: ReactiveHtmlElement[html.Div] =
      div(
        $triggerState.changes
          .withCurrentValueOf($soundStatus)
          .withCurrentValueOf($selectedComponent)
          .map {
            case ((counter, soundStatus), selectedComponent) =>
              (counter, soundStatus, selectedComponent)
          } --> counterAndSoundStatusObserver,
        conditionallyDisplay(exercise, $selectedComponent),
        Components.BrokenLoginPrompt(storage),
        exerciseCounter.behavior,
        exerciseCounter.exerciseServerResultsBusEvents
          .map[CounterAction](result => if (result != 0) ResetCount else DoNotUpdate) --> counterActionBus,
        div(
          Components.ExerciseHeader(exercise.humanFriendlyName),
          Components.BlinkyBox($countT, $triggerState),
          div(
            button("Reset",
                   cls := "button is-warning is-rounded is-size-4 my-1",
                   onClick.mapTo(ResetCount) --> counterActionBus)
          ),
          div(
            button(
              "Submit",
              cls := "button is-link is-rounded is-size-2 my-1",
              $countT --> $countVar.writer,
              onClick.map(
                _ => $countVar.now().value
              ) --> exerciseCounter.submissionsWriter
            )
          ),
          div(
            cls("is-size-3 has-text-centered"),
            div("Daily Total:"),
            child <-- exerciseCounter.$exerciseTotal.map(count => div(count.toString))
          ),
          Components.ProgressBar(exerciseCounter.$percentageComplete),
          child <-- Components.FullyLoadedHistory(exercise, storage),
          RepeatingElement().repeatWithInterval(
            1,
            duration
          ) --> clockTicks,
          $triggerState.map[CounterAction](
            counterState => if (counterState == Relaxed) Increment(1) else DoNotUpdate
          ).changes --> counterActionBus
        )
      )

  }

}
