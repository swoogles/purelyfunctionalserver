package repository

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, Uri}

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
object GpsCoordinates {
  object resorts {
    val Breckenridge: GpsCoordinates = GpsCoordinates(39.473219, -106.078417, "Breckenridge")
    val CrestedButte: GpsCoordinates = GpsCoordinates(38.8697, -106.9878, "CrestedButte")
  }
}

trait WeatherApi[F[_]] {
    def get(gpsCoordinates: GpsCoordinates): F[ForeCast]
}
object WeatherApi {
  // TODO Put this in a better spot, that will error out predictably
  val DARK_SKY_TOKEN: String = System.getenv("DARK_SKY_TOKEN")

  final case class WeatherError(e: Throwable) extends RuntimeException
  implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, ForeCast] =
    jsonOf

  def impl[F[_] : Sync](C: Client[F]): WeatherApi[F] = new WeatherApi[F] {
    val dsl = new Http4sClientDsl[F] {}

    import dsl._

    def get(gpsCoordinates: GpsCoordinates): F[ForeCast] = {
      println("token: " + DARK_SKY_TOKEN)
      val parameterisedUri = s"https://api.darksky.net/forecast/" + DARK_SKY_TOKEN + s"/${gpsCoordinates.latitude},${gpsCoordinates.longitude}"
      C.expect[ForeCast](GET(Uri.unsafeFromString(parameterisedUri)))
        .map( forecastWithoutLocationName => forecastWithoutLocationName.copy(location = Some(gpsCoordinates.locationName)))
        .adaptError { case t =>
          println("error: " + t)
          WeatherError(t) } // Prevent Client Json Decoding Failure Leaking
    }
  }
}