package billding

import com.raquo.airstream.core.Observable
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
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
import com.raquo.laminar.nodes.ReactiveElement
import com.raquo.laminar.nodes.ReactiveElement.Base

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

  implicit val backend = FetchBackend()
  implicit val ec = global

  def getCurrentWheelCount():Int = {
    document.getElementById("wheel_count").innerHTML.toInt
  }

  def resetReps() = {
    Main.count = 0
  }

  def safeResetReps() = {
    val confirmed = org.scalajs.dom.window.confirm(s"Are you sure you want to reset the count?")
    if (confirmed)
      resetReps
    else
      println("I won't throw away those sweet reps!")
  }

  def safelyPostQuadSets(count: Int) = {
    val confirmed = org.scalajs.dom.window.confirm(s"Are you sure you want to submit $count quadsets?")
    if (confirmed) {
        postQuadSets(count)
      0
    } else
      println("Fine, I won't do anything then!")
    1
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
        .get(exerciseUri)
        .auth.bearer(storage.getItem("access_token_fromJS"))
    }
    else if (Meta.accessToken.isDefined) {
      println("We queryParameter token. Use it for getting authorized info. Non-ideal.")
        basicRequest
          .get(exerciseUri.param("access_token", Meta.accessToken.get))
          .header(Header.authorization("Bearer", Meta.accessToken.get))
      } else {
      println("We have no token. Request information for public, chaotic user.")
        basicRequest
          .get(exerciseUri)
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
            Main.count = 0
            document.getElementById("counter").innerHTML = Main.count.toString
            document.getElementById("daily_total").innerHTML = Main.dailyTotal.toString
          }
          case Left(failure) => println("Failed to submit quadsets with error: " + failure)
        }
      }
  }
}

object Main {
  var count = 0
  var dailyTotal = 0
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
      count += 1
      document.getElementById("user_instruction").innerHTML = "Relax"
      document.getElementById("counter").innerHTML = count.toString
      document.body.setAttribute("style", "background-color: green")
      if(document.getElementById("play-audio").asInstanceOf[HTMLInputElement].checked) {
        completeSound.play()
      }
    }
  }

  case class RepeatingElement () extends RepeatWithIntervalHelper

  def Counter() = {
    val repeater = RepeatingElement()

    val diffBus = new EventBus[Int]
    val $count: Signal[Int] = diffBus.events.foldLeft(0)((acc, next) =>
      if(next == 0)
        0
        else
      acc + next)
    div(
      div(cls("session-counter"), child.text <-- $count.map(_.toString)),
      button("+", onClick.mapTo(1) --> diffBus),
      button("Reset", onClick.mapTo(0) --> diffBus),
      button("Submit Quad Sets",
      dataAttr("count") <-- $count.map(_.toString),
        inContext( context =>
        onClick.mapTo(ApiInteractions.safelyPostQuadSets(context.ref.attributes.getNamedItem("data-count").value.toInt)) --> diffBus)),

      repeater.repeatWithInterval(1, new FiniteDuration(20, scala.concurrent.duration.SECONDS)) --> diffBus
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

    val appDiv: Div = div(
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
      Counter()
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
    ApiInteractions.postQuadSets(count) // Doing this to get the initial count
    document.body.setAttribute("style", "background-color: green")
    document.getElementById("counter").innerHTML = count.toString
    dom.window.setInterval(() => toggleColor(), 10000)
    document.getElementById("submit_quad_sets")
      .addEventListener("click", (event: Event) => ApiInteractions.safelyPostQuadSets(count))

    document.getElementById("reset_reps")
      .addEventListener("click", (event: Event) => ApiInteractions.safeResetReps())

  }
}
