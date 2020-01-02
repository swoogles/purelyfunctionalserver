package billding


import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import sttp.client.{BodySerializer, FetchBackend, Response, StringBody}
import sttp.model.MediaType

import scala.concurrent.ExecutionContext.global
import scala.scalajs.js.Date

case class DailyQuantizedExercise(id: Option[Long], name: String, day: String, count: Int)

object ApiInteractions {
  import sttp.client._

  implicit val personSerializer: BodySerializer[DailyQuantizedExercise] = { p: DailyQuantizedExercise =>
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

  def getQuadSetHistory() = {
    val constructedUri =
//      uri"https://purelyfunctionalserver.herokuapp.com/exercises"
                uri"http://localhost:8080/exercises" // TODO Make this dynamic across environments
    println("historical uri: " + constructedUri)
    val request = basicRequest
      .get(constructedUri)


    for {
      response: Response[Either[String, String]] <- request.send()
    } {
      response.body match {
        case Right(jsonBody) => {
          document.getElementById("exercise_history").innerHTML = jsonBody
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
      if (jsDate.getUTCMonth() + 1 > 9)
        (jsDate.getUTCMonth() + 1).toString
      else
        "0" + (jsDate.getUTCMonth() + 1).toString

    val daySection =
      if (jsDate.getUTCDate() + 1 > 9)
        (jsDate.getUTCDate() + 1).toString
      else
        "0" + (jsDate.getUTCDate() + 1).toString


    println("About to post FIXED QUADSETS for month: " + monthSection)
    val formattedLocalDate = jsDate.getFullYear().toString + "-" + monthSection + "-" + daySection
      val exercise = DailyQuantizedExercise(id = Some(1), name = "QuadSets", day = formattedLocalDate, count = count)

      val constructedUri =
//        uri"https://purelyfunctionalserver.herokuapp.com/exercises"
            uri"http://localhost:8080/exercises" // TODO Make this dynamic across environments
      println("uri: " + constructedUri)
      val request = basicRequest
        .body(exercise)
        .post(constructedUri)


      println("About to make a request: " + request)
      for {
        response: Response[Either[String, String]] <- request.send()
      } {
        response.body match {
          case Right(jsonBody) => {
            println("jsonBody: " + jsonBody)
            println("jsonBody.toInt: " + jsonBody.toInt)
            Main.dailyTotal = jsonBody.toInt
            println("Resetting current count after successful submission")
            Main.count = 0
            document.getElementById("counter").innerHTML = Main.count.toString
            document.getElementById("daily_total").innerHTML = Main.dailyTotal.toString
            //          println("count: " + jsonBody.asJson.findAllByKey("count").head.asString.get.toInt)
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
    println("I'm flying")
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
