package repository

import java.util.concurrent.Executors

import fs2.Stream
import cats.effect.{ContextShift, IO, Timer}
import org.scalamock.scalatest.MockFactory
//import org.scalatest.{Matchers, WordSpec}
import org.http4s.client.blaze.BlazeClientBuilder
import weather.{ForeCast, GpsCoordinates, WeatherApi}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

//class WeatherApiSpec extends WordSpec with MockFactory with Matchers {
//  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
//  val ecOne = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
//
//  implicit val csOne: ContextShift[IO] = IO.contextShift(ecOne)
////  implicit val ioEffect = IOTests.ioEffectDefaults
//  "WeatherApi" should {
//    "get weather data from Crested butte" in {
//      val weatherOperations: Stream[IO, ForeCast] =
//      for {
//        client <- BlazeClientBuilder[IO](global).stream
//        weatherApi = WeatherApi.impl[IO](client)
//        weatherResult <- Stream.eval(weatherApi.get(GpsCoordinates(38.8697, -106.9878, "CrestedButte")))
//      } yield {
//        weatherResult
//      }
//      val result = weatherOperations.compile.toVector.unsafeRunSync()
//      println("Result: " + result)
//
//    }
//    "get weather data from Breckenridge" in {
//      val weatherOperations: Stream[IO, ForeCast] =
//        for {
//          client <- BlazeClientBuilder[IO](global).stream
//          weatherApi = WeatherApi.impl[IO](client)
//          weatherResult <- Stream.eval(weatherApi.get(GpsCoordinates.resorts.Breckenridge))
//        } yield {
//          weatherResult
//        }
//      val result = weatherOperations.compile.toVector.unsafeRunSync()
//      println("Result: " + result)
//
//    }
//  }
//
//}
//
