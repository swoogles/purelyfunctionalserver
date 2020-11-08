package billding

import java.time.LocalDate

import com.billding.{FULL, OFF, SoundStatus}
import com.billding.exercises.{Exercise, ExerciseGenericWithReps, Exercises}
import com.billding.settings.{Setting, SettingWithValue, UserSettingWithValue}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{document, html}
import org.scalajs.dom.raw.{AudioContext, HTMLInputElement, Storage}
import sttp.model.{Header, Uri}
import sttp.client.circe._

import scala.concurrent.ExecutionContext.global
import io.circe.generic.auto._
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

object ApiInteractions {
  import sttp.client._

  val exerciseUri: Uri = uri"${Meta.host}/exercises"
  val quadSetUri: Uri = uri"${Meta.host}/exercises/QuadSets"
  val armStretchesUri: Uri = uri"${Meta.host}/exercises/arm_stretches"

  implicit val backend = FetchBackend()
  implicit val ec = global

  // TODO Convert this to laminar
  def representQuadSets(quadsets: List[DailyQuantizedExercise]) = {
    import scalatags.JsDom.all._
    div(
      quadsets
        .map(
          quadSet =>
            div(style := "text-weight: bold; background-color: white; font-size: 18pt;")(
              span(quadSet.day + ": "),
              span(quadSet.count)
            )
        )
    )
  }

  def getQuadSetHistoryInUnsafeScalaTagsForm(storage: Storage) = {
    val request = {
      if (storage
            .getItem("access_token_fromJS")
            .nonEmpty) { // We have a stored token. Use it for getting authorized info
        basicRequest
          .get(quadSetUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
      } else if (Meta.accessToken.isDefined) { // We have a queryParameter token. Use it for getting authorized info. Non-ideal.
        basicRequest
          .get(quadSetUri.param("access_token", Meta.accessToken.get))
          .header(Header.authorization("Bearer", Meta.accessToken.get))
      } else { // no token. Request information for public, chaotic user.
        basicRequest
          .get(quadSetUri)
      }
    }

    import scalatags.JsDom.all._
    for {
      response: Response[Either[String, String]] <- request.send()
    } {
      response.body match {
        case Right(jsonBody) => {
          circe.deserializeJson[List[DailyQuantizedExercise]].apply(jsonBody) match {
            case Right(value) => {
              document
                .getElementById("exercise_history")
                // TODO Why do I need the double .render call?
                .appendChild(representQuadSets(value).render.render)
            }
            case Left(failure) => println("Parse failure: " + failure)
          }
        }
        case Left(failure) => {
          println("Failure: " + failure)
        }
      }
    }

  }

  def getUserSetting(setting: Setting): Future[UserSettingWithValue] = {
    val storage = org.scalajs.dom.window.localStorage

    val settingsUri: Uri = uri"${Meta.host}/user_settings/${setting.name}"
    val request =
      if (storage.getItem("access_token_fromJS").nonEmpty) {
        basicRequest
          .get(settingsUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
      } else if (Meta.accessToken.isDefined) {
        basicRequest
          .get(settingsUri.param("access_token", Meta.accessToken.get))
      } else {
        basicRequest
          .get(settingsUri)
      }

    for {
      response: Response[Either[String, String]] <- request.send()
    } yield {

      response.body match {
        case Right(jsonBody) => {
          circe.deserializeJson[UserSettingWithValue].apply(jsonBody) match {
            case Right(value) => {
              value
            }
            case Left(failure) => throw new RuntimeException("Parse failure: " + failure)
          }
        }
        case Left(failure) => {
          throw new RuntimeException("failure: " + failure)
        }
      }
    }
  }

  //todo accept storage as parameter
  def postUserSetting(setting: SettingWithValue): Future[Int] = {
    val storage = org.scalajs.dom.window.localStorage

    val settingsUri: Uri = uri"${Meta.host}/user_settings"
    val request =
      if (storage.getItem("access_token_fromJS").nonEmpty) {
        basicRequest
          .post(settingsUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
          .body(setting)
      } else if (Meta.accessToken.isDefined) {
        basicRequest
          .post(settingsUri.param("access_token", Meta.accessToken.get))
          .body(setting)
      } else {
        basicRequest
          .body(setting)
          .post(settingsUri)
      }

    for {
      response: Response[Either[String, String]] <- request.send()
    } yield {
      response.body match {
        case Right(jsonBody) => {
          jsonBody.toInt
        }
        case Left(failure) => {
          println("Failed to submit armstretches with error: " + failure)
          0
        }
      }
    }
  }

  //todo accept storage as parameter
  def postExerciseSession(count: Int, exerciseName: String): Future[Int] = {
    val localDate = Time.formattedLocalDate()
    val exercise =
      DailyQuantizedExercise(name = exerciseName, day = LocalDate.parse(localDate), count = count)

    val storage = org.scalajs.dom.window.localStorage

    val request =
      if (storage.getItem("access_token_fromJS").nonEmpty) {
        basicRequest
          .post(exerciseUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
          .body(exercise)
      } else if (Meta.accessToken.isDefined) {
        basicRequest
          .body(exercise)
          .post(exerciseUri.param("access_token", Meta.accessToken.get))
      } else {
        basicRequest
          .body(exercise)
          .post(exerciseUri)
      }

    for {
      response: Response[Either[String, String]] <- request.send()
    } yield {
      response.body match {
        case Right(jsonBody) => {
          jsonBody.toInt
        }
        case Left(failure) => {
          println("Failed to submit armstretches with error: " + failure)
          0
        }
      }
    }
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

  def SelectorButton(
    $selectedComponent: Signal[Exercise],
    exercise: Exercise,
    componentSelections: WriteBus[Exercise],
    $exerciseTotal: Signal[Int]
  ) = {
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

  trait ExerciseSessionComponent {
    val exercise: Exercise
    val exerciseSelectButton: ReactiveHtmlElement[html.Div]
    def exerciseSessionComponent(): ReactiveHtmlElement[html.Div]
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

    val $complete: Signal[Boolean] =
      exerciseCounter.$exerciseTotal.map(_ >= exercise.dailyGoal)

    val exerciseSelectButton: ReactiveHtmlElement[html.Div] =
      SelectorButton(
        $selectedComponent,
        exercise: Exercise,
        componentSelections,
        exerciseCounter.$exerciseTotal
      )

    val counterAndSoundStatusObserver = Observer[(Int, SoundStatus, Exercise)] {
      case (currentCount, soundStatus, selectedExercise) =>
        if (soundStatus == FULL && selectedExercise == exercise) {
          if (currentCount == exercise.dailyGoal) soundCreator.goalReached.play()
          else soundCreator.addExerciseSet.play()
        }
    }

    def exerciseSessionComponent(): ReactiveHtmlElement[html.Div] =
      div(
        exerciseCounter.behavior,
        exerciseCounter.$exerciseTotal.changes
          .withCurrentValueOf(soundStatus)
          .withCurrentValueOf($selectedComponent)
          .map {
            case ((counter, soundStatus), selectedComponent) =>
              (counter, soundStatus, selectedComponent)
          } --> counterAndSoundStatusObserver,
        $complete --> updateMonitor,
        conditionallyDisplay(exercise, $selectedComponent),
        cls("centered"),
        (if (storage.getItem("access_token_fromJS") == "public")
           div(
             div("You're not actually logged in!"),
             a(href := "/oauth/login", cls := "button is-link is-rounded is-size-3", "Re-login")
           )
         else
           div()),
        div(
          cls <-- exerciseCounter.$exerciseTotal.map(
            currentCount => if (currentCount >= exercise.dailyGoal) "has-background-success" else ""
          ),
          div(exercise.humanFriendlyName, cls := "is-size-3"),
          div(
            cls("session-counter"),
            div(
              child <-- exerciseCounter.$exerciseTotal.map(count => div(count.toString))
            )
          ),
          div(
            child <-- exerciseCounter.$percentageComplete.map(
              Widgets.progressBar
            )
          )
        ),
        div(
          cls := "centered",
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
        ),
        div(
          cls("rep-explanation"),
          div(
            s"""Session: ${exercise.setsPerSession} set${if (exercise.setsPerSession > 1) "s"
            else ""} of ${exercise.repsPerSet}"""
          ),
          div(s"Daily Goal: ${exercise.dailyGoal} reps")
        ),
        child <-- $complete.map {
          case true  => div("Good job! You reached your daily goal!")
          case false => div("")
        }
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
      SelectorButton(
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

    val duration = new FiniteDuration(1, scala.concurrent.duration.SECONDS)

    def exerciseSessionComponent(): ReactiveHtmlElement[html.Div] =
      div(
        $counterState.changes
          .withCurrentValueOf($soundStatus)
          .withCurrentValueOf($selectedComponent)
          .map {
            case ((counter, soundStatus), selectedComponent) =>
              (counter, soundStatus, selectedComponent)
          } --> counterAndSoundStatusObserver,
        conditionallyDisplay(exercise, $selectedComponent),
        (if (storage.getItem("access_token_fromJS") == "public")
           div("You're not actually logged in!")
         else
           div()),
        exerciseCounter.behavior,
        exerciseCounter.exerciseServerResultsBusEvents
          .map[CounterAction](result => if (result != 0) ResetCount else DoNotUpdate) --> counterActionBus,
        cls := "centered",
        div(
          div(exercise.humanFriendlyName, cls := "is-size-3"),
          div(cls("session-counter"),
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
            styleAttr := "text-align: center; font-size: 2em",
            span("Daily Total:"),
            span(
              child <-- exerciseCounter.$exerciseTotal.map(count => div(count.toString))
            ),
            span(styleAttr := "font-size: 2em")
          ),
          div(
            child <-- exerciseCounter.$percentageComplete.map(
              Widgets.progressBar
            )
          ),
          div(idAttr := "exercise_history"),
          // TODO Look at method to derive this first repeater off of the 2nd
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

  def laminarStuff(storage: Storage) = {
    val allExerciseCounters = Var[Seq[(ExerciseSessionComponent, Boolean)]](Seq())
    val updateMonitor = Observer[(Boolean, Exercise)](onNext = {
      case (isComplete, exercise) => {
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
      }
    })

    val componentSelections = new EventBus[Exercise]
    val $selectedComponent: Signal[Exercise] =
      componentSelections.events
        .foldLeft[Exercise](Exercises.supineShoulderExternalRotation)((_, selection) => selection)

    val soundStatusEventBus = new EventBus[SoundStatus]
    val $soundStatus =
      soundStatusEventBus.events.foldLeft[SoundStatus](OFF) {
        case (oldStatus: SoundStatus, newStatus: SoundStatus) => {
          println("New status in parent: " + newStatus)
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
      cls := "centered",
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
              println("Returned setting with value : " + settingWithValue)
              println("Value: " + settingWithValue.value)
              if (settingWithValue.value == "FULL") FULL else OFF
            }
        ) --> soundStatusEventBus.writer,
      Bulma.menu(
        $completedExercises,
        $incompleteExercises,
        soundStatusEventBus.writer,
        soundStatusEventBus.events.toSignal(OFF)
      ),
      betterExerciseComponents.map(_.exerciseSessionComponent())
    )

    println("going to render laminarApp sunday 10:40")

    dom.document.querySelector("#laminarApp").innerHTML = "" // Ugly emptying method
    render(dom.document.querySelector("#laminarApp"), appDiv)
    // TODO order matters with this unsafe call!!
    ApiInteractions.getQuadSetHistoryInUnsafeScalaTagsForm(storage) // TODO Load this data up for certain pages

  }

  def main(args: Array[String]): Unit = {
    val storage = org.scalajs.dom.window.localStorage
    laminarStuff(storage)

    if (Meta.accessToken.isDefined) {
      dom.window.location.href =
        "https://purelyfunctionalserver.herokuapp.com/resources/html/PhysicalTherapyTracker/index.html"
    }

    import scala.concurrent.ExecutionContext.Implicits.global

//    override def body(): HTMLElement = document.body

    toServiceWorkerNavigator(org.scalajs.dom.window.navigator).serviceWorker
      .register("./sw-opt.js", js.Dynamic.literal(scope = "./"))
      .toFuture
      .onComplete {
        case Success(registration) => {
          println("successful registration!")
          registration.update()
          println("updated registration!")
        }
        case Failure(error) =>
          println(
            s"registerServiceWorker: service worker registration failed > ${error.printStackTrace()}"
          )
      }
  }
}
