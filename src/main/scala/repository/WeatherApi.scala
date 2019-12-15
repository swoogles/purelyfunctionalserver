package repository

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, EntityEncoder, Uri}

case class TimePeriodData(
                           summary: String, // This isn't handling some symbols, like '<'
                           icon:    String
)

case class DataPoint(
                      apparentTemperature: Double,
                      temperature: Double,
                      summary: String,
                      cloudCover: Double,
                      precipIntensity: Double,
                      precipProbability: Double,
                      windSpeed: Double,
                      time: Long
                    )

case class ForeCast (
                      timezone:  String,
                      currently: DataPoint,
                      hourly:     TimePeriodData,
                      daily:     TimePeriodData,
                      location:  Option[String]
                    )

case class GpsCoordinates(
                         latitude: Double,
                         longitude: Double,
                         locationName: String
                         )

trait WeatherApi[F[_]] {
    def get(gpsCoordinates: GpsCoordinates): F[ForeCast]
}
object WeatherApi {
  val DARK_SKY_TOKEN: String = System.getenv("DARK_SKY_TOKEN")

  final case class WeatherError(e: Throwable) extends RuntimeException
  implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, ForeCast] =
    jsonOf
  implicit def commitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ForeCast] =
    jsonEncoderOf

  def impl[F[_] : Sync](C: Client[F]): WeatherApi[F] = new WeatherApi[F] {
    val dsl = new Http4sClientDsl[F] {}

    import dsl._

    def get(gpsCoordinates: GpsCoordinates): F[ForeCast] = {
      val parameterisedUri = s"https://api.darksky.net/forecast/" + DARK_SKY_TOKEN + s"/${gpsCoordinates.latitude},${gpsCoordinates.longitude}"
      println("parameterisedUri: " + parameterisedUri)
      C.expect[ForeCast](GET(Uri.unsafeFromString(parameterisedUri)))
        .map( forecastWithoutLocationName => forecastWithoutLocationName.copy(location = Some(gpsCoordinates.locationName)))
        .adaptError { case t =>
          println("error: " + t)
          WeatherError(t) } // Prevent Client Json Decoding Failure Leaking
    }
  }
}
