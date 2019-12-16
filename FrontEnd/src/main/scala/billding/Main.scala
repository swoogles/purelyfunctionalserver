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

  def postQuadSets(count: Int) = {
    val exercise = DailyQuantizedExercise(id = Some(1), name = "QuadSets", day = LocalDate.now(ZoneId.of("UTC")).toString, count = count)

    val constructedUri = uri"http://localhost:8080/exercises" // TODO Make this dynamic across environments
    println("uri: " + constructedUri)
    val request = basicRequest
      .body(
        exercise
        //                Map(
        //                  "name" -> exercise.name,
        //                  "day" -> exercise.day.toString,
        //                  "count" -> exercise.count)
      )
      .post(constructedUri)


    println("About to make a request: " + request)
    for {
      response: Response[Either[String, String]] <- request.send()
    } {
      println(response.body)
      println(response.headers)
      "hi"
    }
  }
}

object Main {
  var count = 0

  def toggleColor() =
    if (document.body.getAttribute("style").contains("green")) {
      document.body.setAttribute("style", "background-color: red")
    } else {
      count += 1
      document.getElementById("counter").innerHTML = count.toString
      document.body.setAttribute("style", "background-color: green")
    }

  def main(args: Array[String]): Unit = {
    document.body.setAttribute("style", "background-color: green")
    document.getElementById("counter").innerHTML = count.toString
    dom.window.setInterval(() => toggleColor(), 10000)
    document.getElementById("submit_quad_sets")
      .addEventListener("click", (event: Event) => ApiInteractions.postQuadSets(count))

  }
}
