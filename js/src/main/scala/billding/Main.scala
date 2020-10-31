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
case object DoNotUpdate extends CounterAction

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

  trait ExerciseSessionComponent {
    val exercise: Exercise
    def exerciseSelectButton(): ReactiveHtmlElement[html.Div]
    def exerciseSessionComponent(): ReactiveHtmlElement[html.Div]
  }

  class ExerciseSessionComponentWithExternalStatus(
    componentSelections: EventBus[Exercise],
    val exercise: Exercise,
    $selectedComponent: Signal[Exercise],
    postFunc: (Int, String) => Future[Int],
    soundCreator: SoundCreator,
    storage: Storage,
    updateMonitor: Observer[Boolean]
  ) extends ExerciseSessionComponent {

    private val exerciseSubmissions = new EventBus[Int]

    val exerciseServerResultsBus = new EventBus[Int]

    val exerciseServerResults: EventStream[Int] =
      exerciseSubmissions.events
        .map(submission => EventStream.fromFuture(postFunc(submission, exercise.id)))
        .flatten

    private val $exerciseTotal: Signal[Int] =
      exerciseServerResultsBus.events.foldLeft(0)((acc, next) => next)

    val $complete: Signal[Boolean] =
      $exerciseTotal.map(_ >= exercise.dailyGoal)

    private def indicateSelectedButton(
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

    def exerciseSelectButton(): ReactiveHtmlElement[html.Div] =
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

    val countSoundEffectObserver = Observer[Int](
      onNext = // Currently, this will try to play sounds on page load if goals have been reached
        // I don't want that, but "luckily" the page won't play sounds until there's user interaction
        // This gives the desired behavior, but seems a little janky.
        currentCount =>
          if (currentCount == exercise.dailyGoal) soundCreator.goalReached.play()
          else soundCreator.addExerciseSet.play()
    )

    def exerciseSessionComponent(): ReactiveHtmlElement[html.Div] =
      div(
        EventStream
          .fromFuture(postFunc(0, exercise.id)) --> exerciseServerResultsBus,
        exerciseServerResults --> exerciseServerResultsBus,
        $exerciseTotal --> countSoundEffectObserver,
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
          cls <-- $exerciseTotal.map(
            currentCount => if (currentCount >= exercise.dailyGoal) "has-background-success" else ""
          ),
          div(exercise.humanFriendlyName, cls := "is-size-3"),
          div(
            cls("session-counter"),
            div(child <-- $exerciseTotal.map(count => div(count.toString)))
          )
        ),
        div(
          cls := "centered",
          button(
            cls := "button is-link is-rounded is-size-3",
            disabled <--
            $exerciseTotal.map(
              exerciseTotal => (exerciseTotal <= 0)
            ),
            onClick.map(_ => -1) --> exerciseSubmissions,
            "-1"
          ),
          button(
            cls := "button is-link is-rounded is-size-3",
//            onClick.mapTo(value = { postFunc(1, exercise.id); 1 }) --> exerciseSubmissions,
            onClick.map(_ => 1) --> exerciseSubmissions,
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

  case class TickingExerciseCounterComponent(componentSelections: EventBus[Exercise],
                                             exercise: Exercise,
                                             $selectedComponent: Signal[Exercise],
                                             postFunc: (Int, String) => Future[Int],
                                             storage: Storage,
                                             soundCreator: SoundCreator)
      extends ExerciseSessionComponent {

    val exerciseSubmissions = new EventBus[Int]

    val exerciseServerResultsBus = new EventBus[Int]

    val exerciseServerResults: EventStream[Int] =
      exerciseSubmissions.events
        .map(submission => EventStream.fromFuture(postFunc(submission, exercise.id)))
        .flatten

    val $exerciseTotal: Signal[Int] =
      exerciseServerResultsBus.events.foldLeft(0)((acc, next) => next)

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

    private def indicateSelectedButton(
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

    def exerciseSelectButton(): ReactiveHtmlElement[html.Div] =
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
    val counterActionBus = new EventBus[CounterAction]()

    // Doing this to get the initial count
    val $countT: Signal[Counter] =
      counterActionBus.events.foldLeft(Counter(0))((acc, next) => CounterAction.update(next, acc))

    val $countVar: Var[Counter] = Var(Counter(0))

    // todo restore to 10 seconds
    val duration = new FiniteDuration(1, scala.concurrent.duration.SECONDS)

    def exerciseSessionComponent(): ReactiveHtmlElement[html.Div] =
      div(
        conditionallyDisplay(exercise, $selectedComponent),
        (if (storage.getItem("access_token_fromJS") == "public")
           div("You're not actually logged in!")
         else
           div()),
        exerciseServerResults --> exerciseServerResultsBus,
        exerciseServerResultsBus.events
          .map[CounterAction](result => if (result != 0) ResetCount else DoNotUpdate) --> counterActionBus,
        EventStream
          .fromFuture(postFunc(0, exercise.id)) --> exerciseServerResultsBus,
        cls := "centered",
        div(
          styleAttr <-- $color.map(color => s"background: $color"),
          div(cls("session-counter"), child.text <-- $countT.map(_.value.toString)),
          div(
            button(
              "Submit",
              cls := "button is-link is-rounded",
              $countT --> $countVar.writer,
              onClick.map(
                _ => $countVar.now().value
              ) --> exerciseSubmissions
            )
          ),
          div(
            button("Reset",
                   cls := "button is-warning is-rounded is-size-3",
                   onClick.mapTo(ResetCount) --> counterActionBus)
          ),
          div(
            styleAttr := "text-align: center; font-size: 2em",
            span("Daily Total:"),
            span(child <-- $exerciseTotal.map(count => div(count.toString))),
            span(styleAttr := "font-size: 2em")
          ),
          a(href := "/oauth/login", cls := "button is-link is-rounded is-size-3", "Re-login"),
          div(idAttr := "exercise_history"),
          // TODO Look at method to derive this first repeater off of the 2nd
          repeater.repeatWithInterval(
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
                if (currentExerciseComponent.exercise == exercise) {
                  (currentExerciseComponent, isComplete)
                } else (currentExerciseComponent, wasComplete)
              }
            }
        )
      }
    })

    val componentSelections = new EventBus[Exercise]
    val $selectedComponent: Signal[Exercise] =
      componentSelections.events
        .foldLeft[Exercise](Exercises.supineShoulderExternalRotation)((_, selection) => selection)

    val betterExerciseComponents: Seq[ExerciseSessionComponent] =
      Exercises.manuallyCountedExercises
        .map(
          exercise =>
            new ExerciseSessionComponentWithExternalStatus(
              componentSelections,
              exercise,
              $selectedComponent,
              ApiInteractions.postExerciseSession,
              new SoundCreator,
              storage,
              updateMonitor.contramap[Boolean](isComplete => (isComplete, exercise))
            )
        ) :+ TickingExerciseCounterComponent(componentSelections,
                                             Exercises.QuadSets,
                                             $selectedComponent,
                                             ApiInteractions.postExerciseSession,
                                             storage,
                                             new SoundCreator)

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

    val appDiv: Div = div(
      idAttr := "full_laminar_app",
      cls := "centered",
      Bulma.menu($completedExercises, $incompleteExercises),
//      betterExerciseComponents.map(_.exerciseSelectButton()),
      betterExerciseComponents.map(_.exerciseSessionComponent())
    )

    println("going to render laminarApp sunday 10:40")
    render(dom.document.querySelector("#laminarApp"), appDiv)
    // TODO order matters with this unsafe call!!
    ApiInteractions.getQuadSetHistoryInUnsafeScalaTagsForm(storage) // TODO Load this data up for certain pages

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
