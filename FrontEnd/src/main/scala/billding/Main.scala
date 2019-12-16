package billding

import java.time.{LocalDate, ZoneId}

import scala.concurrent.ExecutionContext.global

import java.time.{LocalDate, ZoneId}

import org.scalajs.dom
import dom.{Event, document}
import io.circe.{Decoder, Encoder}
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.generic.JsonCodec
import sttp.client.circe._
import sttp.model.MediaType

import scala.concurrent.ExecutionContext.global

case class DailyQuantizedExercise(id: Option[Long], name: String, day: String, count: Int)

object ApiInteractions {
  import sttp.client._
  //  implicit val fooDecoder: Decoder[Foo] = deriveDecoder
  //  implicit val fooEncoder: Encoder[DailyQuantizedExercise] =
  //    Encoder.forProduct4("id", "name", "day", "count")
  //  ( exercise: DailyQuantizedExercise => (exercise.))

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

  def postQuadSets(count: Int) = {
      val exercise = DailyQuantizedExercise(id = Some(1), name = "QuadSets", day = LocalDate.now(ZoneId.of("UTC")).toString, count = count)

      val constructedUri =
        uri"https://purelyfunctionalserver.herokuapp.com/exercises"
      //      uri"http://localhost:8080/exercises" // TODO Make this dynamic across environments
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
    ApiInteractions.postQuadSets(count) // Doing this to get the initial count
    document.body.setAttribute("style", "background-color: green")
    document.getElementById("counter").innerHTML = count.toString
    dom.window.setInterval(() => toggleColor(), 10000)
    document.getElementById("submit_quad_sets")
      .addEventListener("click", (event: Event) => ApiInteractions.safelyPostQuadSets(count))

    document.getElementById("reset_reps")
      .addEventListener("click", (event: Event) => ApiInteractions.safelyPostQuadSets(count))

  }
}
