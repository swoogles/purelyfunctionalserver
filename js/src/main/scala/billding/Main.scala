package billding

import com.billding.{FULL, OFF, SoundStatus}
import com.billding.exercises.{Exercise, ExerciseGenericWithReps, Exercises}
import com.billding.settings.{Setting, SettingWithValue, UserSettingWithValue}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{document, html, raw}
import org.scalajs.dom.raw.{AudioContext, HTMLInputElement, Storage}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement.Base
import com.raquo.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}
import exercises.DailyQuantizedExercise
import org.scalajs.dom.experimental.serviceworkers.toServiceWorkerNavigator

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js
import scala.util.{Failure, Success}

sealed trait CounterAction
case object ResetCount extends CounterAction
case class Increment(value: Int) extends CounterAction
case object DoNotUpdate extends CounterAction

object Meta {

  val (host, path) =
    document.URL.split("/").splitAt(3) match {
      case (a, b) => (a.mkString("/"), b.mkString("/"))
    }

  val accessToken: Option[String] =
    if (document.URL.contains("?")) {
      val queryParameters =
        document.URL.split('?')(1)
      val tokenWithPossibleHash = queryParameters.replace("access_token=", "")
      val cleanToken =
        if (tokenWithPossibleHash.endsWith("#"))
          tokenWithPossibleHash.dropRight(1)
        else
          tokenWithPossibleHash
      val storage = org.scalajs.dom.window.localStorage
      storage.setItem("access_token_fromJS", cleanToken) // TODO Test this
      Some(cleanToken)
    } else {
      None
    }

}

object Main {
  var audioContext = new AudioContext()

  case class Counter(value: Int)

  object CounterAction {

    def update(counterAction: CounterAction, counter: Counter) =
      counterAction match {
        case ResetCount           => Counter(0)
        case increment: Increment => counter.copy(counter.value + increment.value)
        case DoNotUpdate          => counter
      }
  }

  case class RepeatingElement() extends RepeatWithIntervalHelper

  trait ExerciseSessionComponent {
    val exercise: Exercise
    val exerciseSelectButton: ReactiveHtmlElement[html.Div]
    val exerciseSessionComponent: ReactiveHtmlElement[html.Div]
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
      child <-- exerciseCounter.$percentageComplete.map(
        Widgets.progressBar
      ),
      ControlCounterButtons(exerciseCounter, exercise),
      Components.GoalExplanation(exercise),
      child <-- exerciseCounter.$complete.map {
        case true  => "Good job! You reached your daily goal!"
        case false => ""
      }.map(div(_))
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

  sealed trait CounterState
  case object Firing extends CounterState
  case object Relaxed extends CounterState

  case class TickingExerciseCounterComponent(componentSelections: WriteBus[Exercise],
                                             exercise: Exercise,
                                             $selectedComponent: Signal[Exercise],
                                             postFunc: (Int, String) => Future[Int],
                                             storage: Storage,
                                             soundCreator: SoundCreator,
                                             $soundStatus: Signal[SoundStatus])
      extends ExerciseSessionComponent {

    val exerciseCounter =
      new ServerBackedExerciseCounter(exercise, postFunc)

    val clockTicks = new EventBus[Int]

    val $counterState =
      clockTicks.events.foldLeft[CounterState](Relaxed)(
        (counterState, _) => if (counterState == Relaxed) Firing else Relaxed
      )

    val $color: Signal[String] =
      $counterState.map {
        case Firing  => "red"
        case Relaxed => "green"
      }

    // TODO See if this triggers unwanted noises when soundStatus changes
    val counterAndSoundStatusObserver = Observer[(CounterState, SoundStatus, Exercise)] {
      case (counterState, soundStatus, selectedExercise) => {
        println("should do some noises because the counter and/or soundStatus changed!")
        // TODO properly get this value from the element below, in a streamy fashion
        if (soundStatus == FULL && selectedExercise == exercise) {
          if (counterState == Firing) {
            soundCreator.startSound.play()
          } else {
            soundCreator.endSound.play()
          }
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

    val counterActionBus = new EventBus[CounterAction]()

    // Doing this to get the initial count
    val $countT: Signal[Counter] =
      counterActionBus.events.withCurrentValueOf($selectedComponent).foldLeft(Counter(0)) {
        case (acc, (next, selectedComponent)) =>
          if (selectedComponent == exercise)
            CounterAction.update(next, acc)
          else
            acc
      }

    val $countVar: Var[Counter] = Var(Counter(0))

    val duration = new FiniteDuration(10, scala.concurrent.duration.SECONDS)

    val exerciseSessionComponent: ReactiveHtmlElement[html.Div] =
      div(
        $counterState.changes
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
          div(exercise.humanFriendlyName, cls := "is-size-3"),
          div(cls("is-size-1 has-text-centered"),
              child.text <-- $countT.map(_.value.toString),
              styleAttr <-- $color.map(color => s"background: $color")),
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
          child <-- exerciseCounter.$percentageComplete.map(
            Widgets.progressBar
          ),
          child <-- Components.FullyLoadedHistory(exercise, storage),
          RepeatingElement().repeatWithInterval(
            1,
            duration
          ) --> clockTicks,
          $counterState.map[CounterAction](
            counterState => if (counterState == Relaxed) Increment(1) else DoNotUpdate
          ).changes --> counterActionBus
        )
      )
  }

  def laminarStuff(storage: Storage, appElement: raw.Element) = {
    val allExerciseCounters = Var[Seq[(ExerciseSessionComponent, Boolean)]](Seq())
    val updateMonitor = Observer[(Boolean, Exercise)](onNext = {
      case (isComplete, exercise) =>
        allExerciseCounters.update(
          previousExerciseCounters =>
            previousExerciseCounters.map {
              case (currentExerciseComponent, wasComplete) => {
                val finalCompletionState =
                  if (currentExerciseComponent.exercise == exercise)
                    isComplete
                  else wasComplete

                (currentExerciseComponent, finalCompletionState)
              }
            }
        )
    })

    val componentSelections = new EventBus[Exercise]
    val $selectedComponent: Signal[Exercise] =
      componentSelections.events
        .foldLeft[Exercise](Exercises.supineShoulderExternalRotation)((_, selection) => selection)

    val soundStatusEventBus = new EventBus[SoundStatus]
    val $soundStatus =
      soundStatusEventBus.events.foldLeft[SoundStatus](OFF) {
        case (oldStatus: SoundStatus, newStatus: SoundStatus) => {
          newStatus
        }
      }

    val betterExerciseComponents: Seq[ExerciseSessionComponent] =
      Exercises.manuallyCountedExercises
        .map(
          exercise =>
            new ExerciseSessionComponentWithExternalStatus(
              componentSelections.writer,
              exercise,
              $selectedComponent,
              ApiInteractions.postExerciseSession,
              new SoundCreator, // TODO Encapuslate $soundStatus in SoundCreator
              storage,
              updateMonitor.contramap[Boolean](isComplete => (isComplete, exercise)),
              $soundStatus
            )
        ) :+ TickingExerciseCounterComponent(componentSelections.writer,
                                             Exercises.QuadSets,
                                             $selectedComponent,
                                             ApiInteractions.postExerciseSession,
                                             storage,
                                             new SoundCreator,
                                             $soundStatus)

    allExerciseCounters.set(betterExerciseComponents.map((_, false)))

    val partitionedExercises: Signal[
      (Seq[(ExerciseSessionComponent, Boolean)], Seq[(ExerciseSessionComponent, Boolean)])
    ] =
      allExerciseCounters.signal.map(
        exerciseCounters =>
          exerciseCounters.partition { case (exerciseCounter, complete) => complete }
      )

    val $completedExercises: Signal[Seq[ExerciseSessionComponent]] =
      partitionedExercises.map(_._1.map(_._1))
    val $incompleteExercises: Signal[Seq[ExerciseSessionComponent]] =
      partitionedExercises.map(_._2.map(_._1))

    import scala.concurrent.ExecutionContext.Implicits.global
    val appDiv: Div = div(
      idAttr := "full_laminar_app",
      cls := " has-text-centered",
      soundStatusEventBus.events --> Observer[SoundStatus] { (nextValue: SoundStatus) =>
        println("About to submit a new soundstatus")
        ApiInteractions.postUserSetting(
          SettingWithValue(Setting("SoundStatus"), nextValue.toString)
        )
      },
      EventStream
        .fromFuture(
          ApiInteractions
            .getUserSetting(
              Setting("SoundStatus")
            )
            .map { settingWithValue =>
              if (settingWithValue.value == "FULL") FULL else OFF
            }
        ) --> soundStatusEventBus.writer,
      Bulma.menu(
        $completedExercises,
        $incompleteExercises,
        soundStatusEventBus.writer,
        soundStatusEventBus.events.toSignal(OFF)
      ),
      betterExerciseComponents.map(_.exerciseSessionComponent)
    )

    println("going to render laminarApp sunday 10:40")

    appElement.innerHTML = "" // Ugly emptying method
    render(appElement, appDiv)
  }

  def main(args: Array[String]): Unit = {
    val storage = org.scalajs.dom.window.localStorage
    laminarStuff(storage, dom.document.querySelector("#laminarApp"))

    if (Meta.accessToken.isDefined) {
      dom.window.location.href =
        "https://purelyfunctionalserver.herokuapp.com/resources/html/PhysicalTherapyTracker/index.html"
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    toServiceWorkerNavigator(org.scalajs.dom.window.navigator).serviceWorker
      .register("./sw-opt.js", js.Dynamic.literal(scope = "./"))
      .toFuture
      .onComplete {
        case Success(registration) =>
          registration.update()
        case Failure(error) =>
          println(
            s"registerServiceWorker: service worker registration failed > ${error.printStackTrace()}"
          )
      }
  }
}
