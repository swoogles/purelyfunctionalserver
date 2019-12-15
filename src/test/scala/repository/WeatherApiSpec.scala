package repository

import java.time.Instant
import java.util.concurrent.Executors
import fs2.Stream

import cats.effect.{ContextShift, IO, Timer}
import io.circe
import model.{High, Low, Medium, Todo}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import io.circe.parser.decode
import repository.Github.{Author, Commit, Payload, Repo, Tree, UserActivityEvent}
import io.circe.generic.auto._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class WeatherApiSpec extends WordSpec with MockFactory with Matchers {
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  val ecOne = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  implicit val csOne: ContextShift[IO] = IO.contextShift(ecOne)
//  implicit val ioEffect = IOTests.ioEffectDefaults
  "WeatherApi" should {
    "get weather data" in {
      val weatherOperations: Stream[IO, String] =
      for {
        client <- BlazeClientBuilder[IO](global).stream
        weatherApi = WeatherApi.impl[IO](client)
        weatherResult <- Stream.eval(weatherApi.get(GpsCoordinates(38.8697, -106.9878)))
      } yield {
        weatherResult
      }
      val result = weatherOperations.compile.toVector.unsafeRunSync()
      println("Result: " + result)

    }
  }

}

