package service

import cats.Applicative
import repository.{DataPoint, ForeCast, Github, GpsCoordinates, TimePeriodData, WeatherApi}
import cats.effect.{IO, Sync}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, HttpService, MediaType, Uri}
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._
import fs2.Stream
import org.http4s.headers.{Location, `Content-Type`}
import io.circe.generic.auto._
import org.http4s.circe.{jsonEncoderOf, jsonOf}

class WeatherService[F[_]: Sync](weatherApi: WeatherApi[F]) extends Http4sDsl[F] {
  implicit def commitEntityEncoderD[F[_]: Applicative]: EntityEncoder[F, DataPoint] =
    jsonEncoderOf
  implicit def commitEntityEncoderT[F[_]: Applicative]: EntityEncoder[F, TimePeriodData] =
    jsonEncoderOf
  implicit def commitEntityEncoderF[F[_]: Applicative]: EntityEncoder[F, repository.ForeCast] =
    jsonEncoderOf

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "weather" =>
      Ok(
        Stream.eval(
          weatherApi.get(GpsCoordinates.resorts.Breckenridge)
        ).map(_.asJson.noSpaces)
        ,
        `Content-Type`(MediaType.application.json)
      )
  }
}
