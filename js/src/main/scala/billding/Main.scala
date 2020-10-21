package billding

import java.time.LocalDate

import com.billding.exercises.{Exercise, Exercises}

import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{document, html}
import org.scalajs.dom.raw.{AudioContext, HTMLInputElement, Storage}
import sttp.model.{Header, Uri}
import sttp.client.circe._

import scala.concurrent.ExecutionContext.global
import io.circe.generic.auto._
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}
import exercises.DailyQuantizedExercise

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

sealed trait CounterAction
case object ResetCount extends CounterAction
case class Increment(value: Int) extends CounterAction

object Meta {

  val (host, path) =
    document.URL.split("/").splitAt(3) match {
      case (a, b) => (a.mkString("/"), b.mkString("/"))
    }

  val accessToken: Option[String] = {
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
}

object ApiInteractions {
  import sttp.client._

  val exerciseUri: Uri = uri"${Meta.host}/exercises"
  val quadSetUri: Uri = uri"${Meta.host}/exercises/QuadSets"
  val armStretchesUri: Uri = uri"${Meta.host}/exercises/arm_stretches"

  implicit val backend = FetchBackend()
  implicit val ec = global

  def getCurrentWheelCount(): Int =
    document.getElementById("wheel_count").innerHTML.toInt

  def resetReps() =
    println("not actually resetting the counter anymore...")

  def safeResetReps() = {
    val confirmed = org.scalajs.dom.window.confirm(s"Are you sure you want to reset the count?")
    if (confirmed)
      resetReps
    else
      println("I won't throw away those sweet reps!")
  }

  def safelyPostQuadSets(count: Int, storage: Storage) = {
    val confirmed =
      org.scalajs.dom.window.confirm(s"Are you sure you want to submit $count quadsets?")
    if (confirmed) {
      postQuadSets(count, storage)
      ResetCount
    } else {
      Increment(0)
    }
  }

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

  def postQuadSets(count: Int, storage: Storage) = {
    val localDate = Time.formattedLocalDate()
    val exercise =
      DailyQuantizedExercise(name = "QuadSets", day = LocalDate.parse(localDate), count = count)

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
          Main.dailyTotal = jsonBody.toInt
//          jsonBody --> countEvents
          // TODO Handle inside component, rather than this ugly id-based retrieval
          document.getElementById("daily_total").innerHTML = Main.dailyTotal.toString
          1
        }
        case Left(failure) => {
          println("Failed to submit quadsets with error: " + failure)
          1
        }
      }
    }
  }

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
  var count = 0
  var dailyTotal = 0
  var audioContext = new AudioContext()

  case class Counter(value: Int)

  object CounterAction {

    def update(counterAction: CounterAction, counter: Counter) =
      counterAction match {
        case ResetCount           => Counter(0)
        case increment: Increment => counter.copy(counter.value + increment.value)
      }
  }

  case class RepeatingElement() extends RepeatWithIntervalHelper

  class ExerciseSessionComponentWithExternalStatus(
    componentSelections: EventBus[Exercise],
    val exercise: Exercise,
    $selectedComponent: Signal[Exercise],
    postFunc: (Int, String) => Future[Int],
    soundCreator: SoundCreator,
    updateMonitor: Observer[(Boolean, Exercise)]
  ) {

    private val exerciseSubmissions = new EventBus[Int]

    private val $exerciseTotal: Signal[Int] =
      exerciseSubmissions.events.foldLeft(0)((acc, next) => acc + next)

    private val $res: Signal[Int] =
      Signal
        .fromFuture(postFunc(0, exercise.id))
        .combineWith($exerciseTotal)
        .map {
          case (optResult, latestResult) =>
            if (optResult.isDefined) optResult.get + latestResult else latestResult
        }

    val $complete: Signal[Boolean] =
      $res.map(_ >= exercise.dailyGoal)

    val $completeWithExercise: Signal[(Boolean, Exercise)] =
      $complete.map((_, exercise))

    private def indicateSelectedButton(
      ): Binder[HtmlElement] =
      cls <--
      $selectedComponent.combineWith($res).map {
        case (selectedExercise, currentCount) =>
          "button small " +
          (if (selectedExercise == exercise)
             "is-primary"
           else {
             if (currentCount >= exercise.dailyGoal)
               "is-success is-rounded is-light"
             else
               "is-link is-rounded "
           })
      }

    def exerciseSelectButton(): ReactiveHtmlElement[html.Div] =
      div(
        button(
          child.text <-- $res.map(
            count => exercise.humanFriendlyName + " " + count + "/" + exercise.dailyGoal
          ),
          indicateSelectedButton(),
          onClick.mapTo {
            exercise
          } --> componentSelections
        )
      )

    val countObserver = Observer[Int](
      onNext = // Currently, this will try to play sounds on page load if goals have been reached
        // I don't want that, but "luckily" the page won't play sounds until there's user interaction
        // This gives the desired behavior, but seems a little janky.
        currentCount => if (currentCount == exercise.dailyGoal) soundCreator.goalReached.play()
    )

    def exerciseSessionComponent(): ReactiveHtmlElement[html.Div] =
      div(
        $res --> countObserver,
        $completeWithExercise --> updateMonitor,
        conditionallyDisplay(exercise, $selectedComponent),
        cls("centered"),
        div(exercise.humanFriendlyName, cls := "exercise-title"),
        div(
          cls("session-counter"),
          div(child <-- $res.map(count => div(count.toString)))
        ),
        div(
          cls := "centered",
          button(
            cls := "button is-link is-rounded medium",
            onClick.mapTo(value = { postFunc(-1, exercise.id); -1 }) --> exerciseSubmissions,
            "-1"
          ),
          button(
            cls := "button is-link is-rounded medium",
            onClick.mapTo(value = { postFunc(1, exercise.id); 1 }) --> exerciseSubmissions,
            "+1"
          )
        )
      )

  }

  def ExerciseSessionComponent(
    exercise: Exercise,
    $selectedComponent: Signal[Exercise],
    postFunc: (Int, String) => Future[Int]
  ): ReactiveHtmlElement[html.Div] = {
    val exerciseSubmissions = new EventBus[Int]
    val $exerciseTotal: Signal[Int] =
      exerciseSubmissions.events.foldLeft(0)((acc, next) => acc + next)
    val $res: Signal[Int] =
      Signal
        .fromFuture(postFunc(0, exercise.id))
        .combineWith($exerciseTotal)
        .map {
          case (optResult, latestResult) =>
            if (optResult.isDefined) optResult.get + latestResult else latestResult
        }

    div(
      conditionallyDisplay(exercise, $selectedComponent),
      cls("centered"),
      div(
        cls("session-counter"),
        div(child <-- $res.map(count => div(count.toString)))
      ),
      div(
        cls := "centered",
        button(
          cls := "button is-link is-rounded medium",
          onClick.mapTo(value = { postFunc(-1, exercise.id); -1 }) --> exerciseSubmissions,
          "-1"
        ),
        button(
          cls := "button is-link is-rounded medium",
          onClick.mapTo(value = { postFunc(1, exercise.id); 1 }) --> exerciseSubmissions,
          "+1"
        )
      )
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

  def TickingExerciseCounterComponent(id: Exercise,
                                      $selectedComponent: Signal[Exercise],
                                      storage: Storage,
                                      soundCreator: SoundCreator): ReactiveHtmlElement[html.Div] = {
    val repeater = RepeatingElement()

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

    val noises = $counterState.map(counterState => {
      // TODO properly get this value from the element below, in a streamy fashion
      if (document.getElementById("play-audio").asInstanceOf[HTMLInputElement].checked) {
        if (counterState == Firing) {
          soundCreator.startSound.play()
        } else {
          soundCreator.endSound.play()
        }
      }
    })
    val counterActionBus = new EventBus[CounterAction]()

    // Doing this to get the initial count
    val $countT: Signal[Counter] =
      counterActionBus.events.foldLeft(Counter(0))((acc, next) => CounterAction.update(next, acc))

    val $countVar: Var[Counter] = Var(Counter(0))

    val duration = new FiniteDuration(10, scala.concurrent.duration.SECONDS)

    div(
      conditionallyDisplay(id, $selectedComponent),
      cls := "centered",
      div(
        styleAttr <-- $color.map(color => s"background: $color"),
        div(cls("session-counter"), child.text <-- $countT.map(_.value.toString)),
        div(
          button(
            "Submit",
            cls := "button is-link is-rounded",
            $countT --> $countVar.writer,
            onClick.mapTo(
              value = ApiInteractions.safelyPostQuadSets(
                $countVar.now().value,
                storage
              )
            ) --> counterActionBus
          )
        ),
        div(
          button("Reset",
                 cls := "button is-warning is-rounded medium",
                 onClick.mapTo(ResetCount) --> counterActionBus)
        ),
        div(
          styleAttr := "font-size: 4em",
          cls := "electox",
          span("Play Sounds:"),
          input(typ := "checkbox", idAttr := "play-audio", name := "play-audio", value := "true")
        ),
        div(
          styleAttr := "text-align: center; font-size: 2em",
          span("Daily Total:"),
          span(idAttr := "daily_total"),
          span(styleAttr := "font-size: 2em")
        ),
        a(href := "/oauth/login", cls := "button is-link is-rounded medium", "Re-login"),
        div(idAttr := "exercise_history"),
        // TODO Look at method to derive this first repeater off of the 2nd
        repeater.repeatWithInterval(
          Increment(1).asInstanceOf[CounterAction],
          duration * 2
        ) --> counterActionBus,
        repeater.repeatWithInterval(
          1,
          duration
        ) --> clockTicks
      )
    )
  }

  def laminarStuff(storage: Storage) = {
    ApiInteractions.getQuadSetHistoryInUnsafeScalaTagsForm(storage) // TODO Load this data up for certain pages
    ApiInteractions.postQuadSets(0, storage)

    val componentSelections = new EventBus[Exercise]
    val $selectedComponent: Signal[Exercise] =
      componentSelections.events
        .foldLeft[Exercise](Exercises.supineShoulderExternalRotation)((_, selection) => selection)

    def indicateSelectedButton(
      exerciseOfCurrentComponent: Exercise
    ): Binder[HtmlElement] =
      cls <--
      $selectedComponent.map { selectedComponent: Exercise =>
        "button small " +
        (if (selectedComponent == exerciseOfCurrentComponent)
           "is-primary"
         else
           "is-link is-rounded ")
      }

    def exerciseSelectButton(exercise: Exercise) =
      div(
        button(
          exercise.humanFriendlyName,
          indicateSelectedButton(exercise),
          onClick.mapTo {
            exercise
          } --> componentSelections
        )
      )

    val allExerciseCounters = Var[Seq[(ExerciseSessionComponentWithExternalStatus, Boolean)]](Seq())
    val updateMonitor = Observer[(Boolean, Exercise)](onNext = {
      case (isComplete, exercise) => {
        allExerciseCounters.update(
          previousExercises =>
            previousExercises.map {
              case (currentExerciseComponent, wasComplete) => {
                if (currentExerciseComponent.exercise == exercise) {
                  (currentExerciseComponent, isComplete)
                } else (currentExerciseComponent, wasComplete)
              }
            }
        )
      }
    })

    val betterExerciseComponents: Seq[ExerciseSessionComponentWithExternalStatus] =
      Exercises.manuallyCountedExercises
        .map(
          exercise =>
            new ExerciseSessionComponentWithExternalStatus(
              componentSelections,
              exercise,
              $selectedComponent,
              ApiInteractions.postExerciseSession,
              new SoundCreator,
              updateMonitor
            )
        )

    allExerciseCounters.set(betterExerciseComponents.map((_, false)))

    val partitionedExercises: Signal[
      (Seq[(ExerciseSessionComponentWithExternalStatus, Boolean)],
       Seq[(ExerciseSessionComponentWithExternalStatus, Boolean)])
    ] =
      allExerciseCounters.signal.map(
        exerciseCounters =>
          exerciseCounters.partition { case (exerciseCounter, complete) => complete }
      )

    val $completedExercises: Signal[Seq[ExerciseSessionComponentWithExternalStatus]] =
      partitionedExercises.map(_._1.map(_._1))
    val $incompleteExercises: Signal[Seq[ExerciseSessionComponentWithExternalStatus]] =
      partitionedExercises.map(_._2.map(_._1))

    class ComponentCollection(components: Seq[ComponentX]) {
      val completedComponents: Signal[Seq[ComponentX]] = ???
      val incompleteComponents: Signal[Seq[ComponentX]] = ???
    }

    class ComponentX(val $complete: Signal[Boolean])

    class ExercisesRegimen(
      betterExerciseComponents: Seq[ExerciseSessionComponentWithExternalStatus]
    ) {
      val res: Seq[Signal[Option[ExerciseSessionComponentWithExternalStatus]]] =
        betterExerciseComponents.map(
          exercise => exercise.$complete.map(if (_) Some(exercise) else None)
        )

      val completions: Signal[Boolean] =
        betterExerciseComponents
          .map(exercise => exercise.$complete)
          .foldLeft(Val(true).asInstanceOf[Signal[Boolean]]) {
            case (a: Val[Boolean], b: Signal[Boolean]) =>
              a.combineWith(b).map { case (aRes, bRes) => aRes || bRes }
          }

      val updates: EventStream[Boolean] =
        betterExerciseComponents
          .map(exercise => exercise.$complete.changes)
          .reduce(EventStream.merge(_, _))

      val completedExercises: Signal[Seq[ExerciseSessionComponentWithExternalStatus]] = ???
    }

    val appDiv: Div = div(
      idAttr := "full_laminar_app",
      cls := "centered",
      Bulma.menu(
        $completedExercises,
        $incompleteExercises,
        betterExerciseComponents
          .map(_.exerciseSelectButton()),
        exerciseSelectButton(Exercises.QuadSets)
      ),
//      betterExerciseComponents.map(_.exerciseSelectButton()),
      TickingExerciseCounterComponent(Exercises.QuadSets,
                                      $selectedComponent,
                                      storage,
                                      new SoundCreator),
      betterExerciseComponents.map(_.exerciseSessionComponent())
    )

    println("going to render laminarApp tuesday 10:06")
    render(dom.document.querySelector("#laminarApp"), appDiv)
  }

  def main(args: Array[String]): Unit = {
    val storage = org.scalajs.dom.window.localStorage
    laminarStuff(storage)

    if (Meta.accessToken.isDefined) {
      dom.window.location.href =
        "https://purelyfunctionalserver.herokuapp.com/resources/html/index.html"
    }
  }
}
