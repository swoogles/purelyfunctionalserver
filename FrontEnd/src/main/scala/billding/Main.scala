package billding


import io.circe.AccumulatingDecoder.Result
import io.circe.{Decoder, HCursor}
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import sttp.client.{BodySerializer, FetchBackend, Response, StringBody}
import sttp.model.{MediaType, Uri}

import scala.concurrent.ExecutionContext.global
import scala.scalajs.js.Date

import io.circe._, io.circe.generic.semiauto._

case class DailyQuantizedExercise(id: Option[Long], name: String, day: String, count: Int)

object ApiInteractions {
  import sttp.client._

  val (host, path) =
    document.URL.split("/").splitAt(3) match {
      case (a, b) => (a.mkString("/"), b.mkString("/"))
    }

  val exerciseUri: Uri = uri"${host}/exercises"
  implicit val decoder: Decoder[DailyQuantizedExercise] =  deriveDecoder[DailyQuantizedExercise]
//    new Decoder[DailyQuantizedExercise] {
//    override def apply(c: HCursor): Decoder.Result[DailyQuantizedExercise] =  {
//      c.values.foreach("Println value: " + _)
//      ???
//  }
//}

  implicit val personSerializer: BodySerializer[DailyQuantizedExercise] = {

import io.circe.Decoder.Result
import io.circe.HCursor

p: DailyQuantizedExercise =>
    val serialized =
      s"""{
         |  "name" : "${p.name}",
         |  "day" : "${p.day}",
         |  "count" : ${p.count}
         |}  """.stripMargin
    StringBody(serialized, "UTF-8", Some(MediaType.ApplicationJson))
  }

  // the `query` parameter is automatically url-encoded
  // `sort` is removed, as the value is not defined

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
        .map(quadSet=> div(
          span(quadSet.day),
          span(quadSet.count),
        ))
    )

  def getQuadSetHistory() = {
    val request = basicRequest
      .get(exerciseUri)


    for {
      response: Response[Either[String, String]] <- request.send()
    } {
      response.body match {
        case Right(jsonBody) => {
          circe.deserializeJson[List[DailyQuantizedExercise]].apply(jsonBody) match {
            case Right(value) => {
              document.getElementById("exercise_history").innerHTML =
                representQuadSets(value).render.render.toString
            }
            case Left(failure) => println("Parse failure: "+ failure)
          }

          println("jsonBody: " + jsonBody)
        }
        case Left(failure) => {
          println("Failure: " + failure)
        }
      }
      println(response.headers)
      "hi"
    }

  }

  def postQuadSets(count: Int) = {
    val jsDate = new Date()
    val monthSection =
      if (jsDate.getMonth() + 1 > 9)
        (jsDate.getMonth() + 1).toString
      else
        "0" + (jsDate.getMonth() + 1).toString

    val daySection =
      if (jsDate.getDate() + 1 > 9)
        (jsDate.getDate() + 1).toString
      else
        "0" + (jsDate.getDate() + 1).toString

    println("Current hours: " + jsDate.getHours())
    println("Full Date: " + jsDate)

    val formattedLocalDate = jsDate.getFullYear().toString + "-" + monthSection + "-" + daySection
      val exercise = DailyQuantizedExercise(id = Some(1), name = "QuadSets", day = formattedLocalDate, count = count)

      val request = basicRequest
        .body(exercise)
        .post(exerciseUri)


      println("About to make a request: " + request)
      for {
        response: Response[Either[String, String]] <- request.send()
      } {
        response.body match {
          case Right(jsonBody) => {
            println("jsonBody: " + jsonBody)
            println("jsonBody.toInt: " + jsonBody.toInt)
            Main.dailyTotal = jsonBody.toInt
            Main.count = 0
            document.getElementById("counter").innerHTML = Main.count.toString
            document.getElementById("daily_total").innerHTML = Main.dailyTotal.toString
          }
          case Left(failure) => println("Failed to submit quadsets with error: " + failure)
        }
        println(response.headers)
        "hi"
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
