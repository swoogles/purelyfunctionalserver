package repository
import java.time.format.DateTimeParseException

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, EntityEncoder, Uri}
import cats.syntax.either._
// import cats.syntax.either._

import io.circe.{ Decoder, Encoder }
// import io.circe.{Decoder, Encoder}

import java.time.Instant
// import java.time.Instant

case class TimePeriodData(
                           summary: String, // This isn't handling some symbols, like '<'
                           icon:    String
)

case class DataPoint(
// ApparentTemperature float64
// Temperature         float64
// Summary             string
// CloudCover          float64
// PrecipIntensity     float64
// PrecipProbability   float64
// WindSpeed           float64
// Time                int64

                    )

case class ForeCast (
                      timezone:  String,
                      currently: DataPoint,
                      hourly:     TimePeriodData,
                      daily:     TimePeriodData,
                      location:  String
                    )

case class GpsCoordinates(
                         latitude: Double,
                         longitude: Double
                         )

trait WeatherApi[F[_]] {
    def get(gpsCoordinates: GpsCoordinates): F[String]
}
object WeatherApi {
  val DARK_SKY_TOKEN: String = System.getenv("DARK_SKY_TOKEN")

  final case class WeatherError(e: Throwable) extends RuntimeException

  def impl[F[_] : Sync](C: Client[F]): WeatherApi[F] = new WeatherApi[F] {
    val dsl = new Http4sClientDsl[F] {}

    import dsl._

    def get(gpsCoordinates: GpsCoordinates): F[String] = {
      val parameterisedUri = s"https://api.darksky.net/forecast/" + DARK_SKY_TOKEN + s"/${gpsCoordinates.latitude},${gpsCoordinates.longitude}"
      C.expect[String](GET(Uri.unsafeFromString(parameterisedUri)))
        .adaptError { case t => WeatherError(t) } // Prevent Client Json Decoding Failure Leaking
    }
  }
}
