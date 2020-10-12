package billding

import java.time.LocalDate

import billding.Main.{Increment, ResetCount}
import com.raquo.airstream.core.Observable
import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{document, html, Event}
import org.scalajs.dom.raw.{AudioContext, Element, HTMLAudioElement, HTMLInputElement, Storage}
import sttp.model.{Header, Uri}
import sttp.client.circe._

import scala.concurrent.ExecutionContext.global
import scala.scalajs.js.{Date, URIUtils}
import io.circe.generic.auto._
import io.circe.syntax._
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}
import com.raquo.laminar.nodes.ReactiveElement.Base
import exercises.DailyQuantizedExercise

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

sealed trait Exercise {
  val id: String
  val humanFriendlyName: String
}

case object QuadSets extends Exercise {
  val id: String = "QuadSets"
  val humanFriendlyName = "QuadSets"
}

case object ShoulderStretches extends Exercise {
  val id: String = "shoulder_stretches"
  val humanFriendlyName = "Shoulder Stretches"
}

case object ShoulderSqueezes extends Exercise {
  val id: String = "shoulder_squeezes"
  val humanFriendlyName = "Shoulder Squeezes"
}

object Time {

  def formattedLocalDate(): String = {
    val jsDate = new Date()
    val monthSection =
      if (jsDate.getMonth() + 1 > 9)
        (jsDate.getMonth() + 1).toString
      else
        "0" + (jsDate.getMonth() + 1).toString

    val daySection =
      if (jsDate.getDate() > 9)
        jsDate.getDate().toString
      else
        "0" + jsDate.getDate().toString

    jsDate.getFullYear().toString + "-" + monthSection + "-" + daySection
  }

}

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

  def getQuadSetHistory(storage: Storage) = {
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
    } {
      response.body match {
        case Right(jsonBody) => {
          Main.dailyTotal = jsonBody.toInt
          document.getElementById("daily_total").innerHTML = Main.dailyTotal.toString
        }
        case Left(failure) => println("Failed to submit quadsets with error: " + failure)
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
  sealed trait CounterAction
  case object ResetCount extends CounterAction
  case class Increment(value: Int) extends CounterAction

  object CounterAction {

    def update(counterAction: CounterAction, counter: Counter) =
      counterAction match {
        case ResetCount           => Counter(0)
        case increment: Increment => counter.copy(counter.value + increment.value)
      }
  }

  case class RepeatingElement() extends RepeatWithIntervalHelper

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
        div(cls := "medium", exercise.humanFriendlyName),
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

  def CounterComponent(id: Exercise,
                       $selectedComponent: Signal[Exercise],
                       storage: Storage,
                       soundCreator: SoundCreator): ReactiveHtmlElement[html.Div] = {
    val repeater = RepeatingElement()

    val clockTicks = new EventBus[Int]

    sealed trait CounterState
    case object Firing extends CounterState
    case object Relaxed extends CounterState

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
    val diffBusT = new EventBus[CounterAction]()
    val $countT: Signal[Counter] =
      diffBusT.events.foldLeft(Counter(0))((acc, next) => CounterAction.update(next, acc))

    val duration = new FiniteDuration(10, scala.concurrent.duration.SECONDS)

    div(
      conditionallyDisplay(id, $selectedComponent),
      cls := "centered",
      div(
        dataAttr("noise") <-- noises.observable.map(_ => "noise!"),
        styleAttr <-- $color.map(color => s"background: $color"),
        div(cls("session-counter"), child.text <-- $countT.map(_.value.toString)),
        div(
          button(
            "Submit",
            cls := "button is-link is-rounded",
            dataAttr("count") <-- $countT.map(_.value.toString),
            inContext(
              context =>
                onClick.mapTo(
                  value = ApiInteractions.safelyPostQuadSets(
                    context.ref.attributes.getNamedItem("data-count").value.toInt,
                    storage
                  )
                ) --> diffBusT
            )
          )
        ),
        div(
          button("Reset",
                 cls := "button is-warning is-rounded medium",
                 onClick.mapTo(ResetCount) --> diffBusT)
        ),
        div(
          styleAttr := "font-size: 4em",
          cls := "box",
          span("Play Sounds:"),
          input(typ := "checkbox", idAttr := "play-audio", name := "play-audio", value := "true")
        ),
        div(
          idAttr := "daily_total_section",
          styleAttr := "text-align: center; font-size: 2em",
          span("Daily Total:"),
          span(idAttr := "daily_total", styleAttr := "font-size: 2em")
        ),
        a(href := "/", cls := "button is-link is-rounded medium", "Re-login"),
        div(idAttr := "exercise_history"),
        repeater.repeatWithInterval(
          Increment(1).asInstanceOf[CounterAction],
          duration * 2
        ) --> diffBusT,
        repeater.repeatWithInterval(
          1,
          duration
        ) --> clockTicks
      )
    )
  }

  def Hello(
    helloNameStream: EventStream[String],
    helloColorStream: EventStream[String]
  ): Div =
    div(
      fontSize := "20px", // static CSS property
      color <-- helloColorStream, // dynamic CSS property
      strong("Hello, "), // static child element with a grandchild text node
      child.text <-- helloNameStream // dynamic child (text node in this case)
    )

  def laminarStuff(storage: Storage) = {
    ApiInteractions.getQuadSetHistory(storage) // TODO Load this data up for certain pages
    ApiInteractions.postQuadSets(0, storage) // Doing this to get the initial count

    val nameBus = new EventBus[String]
    val colorStream: EventStream[String] = nameBus.events.map { name =>
      if (name == "Sébastien") "red" else "unset" // make Sébastien feel special
    }

    val componentSelections = new EventBus[Exercise]
    val $selectedComponent: Signal[Exercise] =
      componentSelections.events.foldLeft[Exercise](QuadSets)((_, selection) => selection)

    def exerciseSelectButton(exercise: Exercise) =
      button(exercise.humanFriendlyName,
             cls := "button is-primary is-rounded small",
             onClick.mapTo(exercise) --> componentSelections)

    val appDiv: Div = div(
      idAttr := "full_laminar_app",
      cls := "centered",
      exerciseSelectButton(QuadSets),
      exerciseSelectButton(ShoulderStretches),
      exerciseSelectButton(ShoulderSqueezes),
//      div(
//        "Please enter your name:",
//        input(
//          typ := "text",
//          inContext(thisNode => onInput.mapTo(thisNode.ref.value) --> nameBus) // extract text entered into this input node whenever the user types in it
//        )
//      ),
//      div(
//        "Please accept our greeting: ",
//        Hello(nameBus.events, colorStream)
//      ),
      CounterComponent(QuadSets, $selectedComponent, storage, new SoundCreator),
      ExerciseSessionComponent(ShoulderStretches,
                               $selectedComponent,
                               ApiInteractions.postExerciseSession),
      ExerciseSessionComponent(ShoulderSqueezes,
                               $selectedComponent,
                               ApiInteractions.postExerciseSession)
    )

    println("going to render laminarApp sunday 10:05")
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
