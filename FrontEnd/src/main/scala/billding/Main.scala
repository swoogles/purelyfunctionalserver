package billding

import billding.Main.{Increment, ResetCount}
import com.raquo.airstream.core.Observable
import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.{Event, document, html}
import org.scalajs.dom.raw.Element
import org.scalajs.dom.raw.HTMLAudioElement
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.dom.raw.AudioContext
import sttp.model.{Header, Uri}
import sttp.client.circe._

import scala.concurrent.ExecutionContext.global
import scala.scalajs.js.{Date, URIUtils}
import io.circe.generic.auto._
import io.circe.syntax._
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}
import com.raquo.laminar.nodes.ReactiveElement.Base

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class DailyQuantizedExercise(name: String, day: String, count: Int)

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

    println("Current hours: " + jsDate.getHours())
    println("Full Date: " + jsDate)

    jsDate.getFullYear().toString + "-" + monthSection + "-" + daySection
  }

}

object Meta {
  val (host, path) =
    document.URL.split("/").splitAt(3) match {
      case (a, b) => (a.mkString("/"), b.mkString("/"))
    }

  val accessToken = {
      if (document.URL.contains("?")) {
        val queryParameters =
          document.URL.split('?')(1)
        val tokenWithPossibleHash = queryParameters.replace("access_token=", "")
        val cleanToken =
        if(tokenWithPossibleHash.endsWith("#"))
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

  def getCurrentWheelCount():Int = {
    document.getElementById("wheel_count").innerHTML.toInt
  }

  def resetReps() = {
    println("not actually resetting the counter anymore...")
  }

  def safeResetReps() = {
    val confirmed = org.scalajs.dom.window.confirm(s"Are you sure you want to reset the count?")
    if (confirmed)
      resetReps
    else
      println("I won't throw away those sweet reps!")
  }

    def postQuadSetsTyped(count: Int): Main.CounterAction =
      safelyPostQuadSets(count) match {
        case 0 => ResetCount
        case 1 => Increment(0) // todo better value
        case other => throw new RuntimeException("ouch. bad status code from safelyPostQuadSets")
      }

  def safelyPostQuadSets(count: Int) = {
    val confirmed = org.scalajs.dom.window.confirm(s"Are you sure you want to submit $count quadsets?")
    if (confirmed) {
        postQuadSets(count)
      0
    } else {
      println("Fine, I won't do anything then!")
      1
    }
  }

  import scalatags.JsDom.all._
  def representQuadSets(quadsets: List[DailyQuantizedExercise]) =
    div(
      quadsets
        .map(quadSet=> div(style := "text-weight: bold; background-color: white; font-size: 18pt;")(
          span(quadSet.day + ": "),
          span(quadSet.count),
        ))
    )

  def getQuadSetHistory() = {

    val storage = org.scalajs.dom.window.localStorage
    val request = {
    if (storage.getItem("access_token_fromJS").nonEmpty) {
      println("We have a stored token. Use it for getting authorized info")
      basicRequest
        .get(quadSetUri)
        .auth.bearer(storage.getItem("access_token_fromJS"))
    }
    else if (Meta.accessToken.isDefined) {
      println("We queryParameter token. Use it for getting authorized info. Non-ideal.")
        basicRequest
          .get(quadSetUri.param("access_token", Meta.accessToken.get))
          .header(Header.authorization("Bearer", Meta.accessToken.get))
      } else {
      println("We have no token. Request information for public, chaotic user.")
        basicRequest
          .get(quadSetUri)
      }
    }


    for {
      response: Response[Either[String, String]] <- request.send()
    } {
      response.body match {
        case Right(jsonBody) => {
          circe.deserializeJson[List[DailyQuantizedExercise]].apply(jsonBody) match {
            case Right(value) => {
              document.getElementById("exercise_history")
                // TODO Why do I need the double .render call?
                .appendChild(representQuadSets(value).render.render)
            }
            case Left(failure) => println("Parse failure: "+ failure)
          }
        }
        case Left(failure) => {
          println("Failure: " + failure)
        }
      }
    }

  }

  def postQuadSets(count: Int) = {
    val localDate = Time.formattedLocalDate()
      val exercise = DailyQuantizedExercise(name = "QuadSets", day = localDate, count = count)

    val storage = org.scalajs.dom.window.localStorage
    val request =
      if (storage.getItem("access_token_fromJS").nonEmpty) {
        println("We have a stored token. Use it to post authorized info")
        basicRequest
          .post(exerciseUri)
          .auth.bearer(storage.getItem("access_token_fromJS"))
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

    println("About to make a request: " + request)
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
    def postArmStretchSession(count: Int): Future[Int] = {
      val localDate = Time.formattedLocalDate()
      val exercise = DailyQuantizedExercise(name = "shoulder_stretches", day = localDate, count = count)

      val storage = org.scalajs.dom.window.localStorage
      val request =
        if (storage.getItem("access_token_fromJS").nonEmpty) {
          println("We have a stored token. Use it to post authorized info")
          basicRequest
            .post(exerciseUri)
            .auth.bearer(storage.getItem("access_token_fromJS"))
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

      println("About to make a request: " + request)
      for {
        response: Response[Either[String, String]] <- request.send()
      } yield {
        response.body match {
          case Right(jsonBody) => {
            Main.shoulderStretchTotal = jsonBody.toInt
            document.getElementById("shoulder_stretches_daily_total").innerHTML = jsonBody.toInt.toString
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
  var shoulderStretchTotal = 0
  var audioContext = new AudioContext()

  def sound(src: String): HTMLAudioElement = {
    val sound: HTMLAudioElement = document.createElement("audio").asInstanceOf[HTMLAudioElement]
    sound.src = src
    sound.setAttribute("preload", "auto")
    sound.setAttribute("controls", "none")
    sound.style.display = "none"
    document.body.appendChild(sound)
//    this.play = function(){
//      this.sound.play();
//    }
//    this.stop = function(){
//      this.sound.pause();
//    }
    sound
  }

  val startSound = sound("/resources/audio/startQuadSet/metronome_tock.wav");
  val completeSound = sound("/resources/audio/completeQuadSet/metronome_tink.wav");

  def toggleColor() = {
    if (document.body.getAttribute("style").contains("green")) {
      document.body.setAttribute("style", "background-color: red")
      document.getElementById("user_instruction").innerHTML = "Fire Quad!"
      if(document.getElementById("play-audio").asInstanceOf[HTMLInputElement].checked) {
        startSound.play()
      }
    } else {
      document.getElementById("user_instruction").innerHTML = "Relax"
      document.body.setAttribute("style", "background-color: green")
      if(document.getElementById("play-audio").asInstanceOf[HTMLInputElement].checked) {
        completeSound.play()
      }
    }
  }
  case class Counter(value: Int)
  sealed trait CounterAction
  case object ResetCount extends CounterAction
  case class Increment(value: Int) extends CounterAction

  object CounterAction {
    def update(counterAction: CounterAction, counter: Counter) =
      counterAction match {
        case ResetCount => Counter(0)
        case increment: Increment => counter.copy(counter.value+increment.value)
      }
  }

  case class RepeatingElement () extends RepeatWithIntervalHelper

  def ArmStretchComponent(id: Int, displayCode: Binder[HtmlElement]) = {
    val clockTicks = new EventBus[Int]
    val callbackResult = Signal.fromFuture(ApiInteractions.postArmStretchSession(0))
    val $shoulderStretchTotal: Signal[Int] = clockTicks.events.foldLeft(0)((acc, next) => acc+next)
    val $res: Signal[Int] =
      callbackResult.combineWith($shoulderStretchTotal)
          .map{ case (optResult, latestResult) => if(optResult.isDefined) optResult.get + latestResult else latestResult}

    div(
      button(
        cls := "button is-link is-rounded",
        onClick.mapTo(value ={ApiInteractions.postArmStretchSession(1); 1})  --> clockTicks,
        "Submit",
      ),
      div(
        span(
          "Total:"),
        span(idAttr:="shoulder_stretches_daily_total",
          child <-- $res.map(count => div(count.toString)))
      )
    )

  }

  def CounterComponent(id: Int, displayCode: Binder[HtmlElement]): ReactiveHtmlElement[html.Div] = {
    val repeater = RepeatingElement()

    val clockTicks = new EventBus[Int]
    val $color: Signal[String] = clockTicks.events.foldLeft("green")((color, _) => if(color=="red") "green" else "red")
    val noises = $color.map(color => {
      if(color == "green" && document.getElementById("play-audio").asInstanceOf[HTMLInputElement].checked) {
        startSound.play()
      }
      else if(document.getElementById("play-audio").asInstanceOf[HTMLInputElement].checked) {
        completeSound.play()
      }
    })
    val diffBusT =  new EventBus[CounterAction]()
    val $countT: Signal[Counter] = diffBusT.events.foldLeft(Counter(0))((acc, next) =>
      CounterAction.update(next, acc)
    )

    val duration = new FiniteDuration(10, scala.concurrent.duration.SECONDS)

    div(
      displayCode,
      idAttr:=s"counter_component_$id",
      cls:="centered",
      div(
      dataAttr("noise") <-- noises.observable.map(_ => "noise!"),
      styleAttr <-- $color.map(color=> s"background: $color"),
      div(cls("session-counter"), child.text <-- $countT.map(_.value.toString)),
      button("Submit",
        cls := "button is-link is-rounded",
        dataAttr("count") <-- $countT.map(_.value.toString),
        inContext( context =>
          onClick.mapTo(value =
            ApiInteractions.postQuadSetsTyped(
//              $countT.observe(ownerDiv).now().value)) --> diffBusT)),
              context.ref.attributes.getNamedItem("data-count").value.toInt)) --> diffBusT)),
      button("Reset",
        cls := "button is-warning is-rounded",
        onClick.mapTo(ResetCount) --> diffBusT),
      div(styleAttr:="font-size: 4em", cls:="box",
        span(
        "Play Sounds:"),
        input(typ:="checkbox",idAttr:="play-audio",name:="play-audio",value:="true")
      ),
      div(idAttr:="daily_total_section", styleAttr:="text-align: center; font-size: 2em",
        span("Daily Total:"),
        span(idAttr:="daily_total", styleAttr:="font-size: 2em")
      ),
      a(href:="/", cls := "button is-link is-rounded", "Re-login"),
      div(idAttr:="exercise_history"),

      repeater.repeatWithInterval(
        Increment(1).asInstanceOf[CounterAction],
        duration*2
//        new FiniteDuration(20, scala.concurrent.duration.SECONDS) // todo restore after dev
    ) --> diffBusT,
        repeater.repeatWithInterval(
      1,
          duration
      //        new FiniteDuration(20, scala.concurrent.duration.SECONDS) // todo restore after dev
    ) --> clockTicks
    )
    )
  }

  def Hello(
             helloNameStream: EventStream[String],
             helloColorStream: EventStream[String]
           ): Div = {
    div(
      fontSize := "20px", // static CSS property
      color <-- helloColorStream, // dynamic CSS property
      strong("Hello, "), // static child element with a grandchild text node
      child.text <-- helloNameStream // dynamic child (text node in this case)
    )
  }

  def laminarStuff() = {
    val nameBus = new EventBus[String]
    val colorStream: EventStream[String] = nameBus.events.map { name =>
      if (name == "Sébastien") "red" else "unset" // make Sébastien feel special
    }

    val componentSelections = new EventBus[Int]
    val $selectedComponent: Signal[Int] = componentSelections.events.foldLeft(1)((_, selection) => selection)

    val displayCondition: Binder[HtmlElement] =
    styleAttr <-- $selectedComponent.map(selection => s"""display: ${if (selection == 1) "inline" else "none" }""")

    val appDiv: Div = div(
      idAttr:="full_laminar_app",
      button("QuadSets",
        cls := "button is-primary is-rounded small",
        onClick.mapTo(1) --> componentSelections),
      button("Shoulder Stretches",
        cls := "button is-primary is-rounded small",
        onClick.mapTo(2) --> componentSelections),
//      h1("User Welcomer 9000"),
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
      CounterComponent(1,
        styleAttr <-- $selectedComponent.map(selection => s"""display: ${if (selection == 1) "inline" else "none" }""") ,
      ),
      ArmStretchComponent(2,
          styleAttr <-- $selectedComponent.map(selection => s"""display: ${if (selection == 2) "inline" else "none" }"""),
        )
    )

    render(dom.document.querySelector("#laminarApp"), appDiv)
  }

  def main(args: Array[String]): Unit = {
    laminarStuff()

    println("Cookie: " + document.cookie)
    val storage = org.scalajs.dom.window.localStorage
    if (Meta.accessToken.isDefined) {
      dom.window.location.href = "https://purelyfunctionalserver.herokuapp.com/resources/html/index.html"
    }
    if (storage.getItem("access_token_fromJS").nonEmpty) {
      println("Still have a token stored after loading the page without query params :)")
      println("Value: " + storage.getItem("access_token_fromJS"))
    }

    ApiInteractions.getQuadSetHistory() // TODO Load this data up for certain pages
    ApiInteractions.postQuadSets(0) // Doing this to get the initial count
  }
}
