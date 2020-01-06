package billding

import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import sttp.model.Uri
import sttp.client.circe._

import scala.concurrent.ExecutionContext.global
import scala.scalajs.js.{Date, URIUtils}
import io.circe.generic.auto._
import io.circe.syntax._

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
        Some(queryParameters.replace("access_token=", ""))
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
    if (confirmed)
      postQuadSets(count)
    else
      println("Fine, I won't do anything then!")
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
    val request =
      if (Meta.accessToken.isDefined) {
        basicRequest
          .get(exerciseUri.param("access_token", Meta.accessToken.get))
    } else {
        basicRequest
          .get(exerciseUri)
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

      basicRequest
        .body(exercise)
        .post(exerciseUri.param("access_token", Meta.accessToken.get))

    val request =
      if (Meta.accessToken.isDefined) {
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

  def toggleColor() =
    if (document.body.getAttribute("style").contains("green")) {
      document.body.setAttribute("style", "background-color: red")
    } else {
      count += 1
      document.getElementById("counter").innerHTML = count.toString
      document.body.setAttribute("style", "background-color: green")
    }

  def main(args: Array[String]): Unit = {
    println("access_token: " + Meta.accessToken)
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
