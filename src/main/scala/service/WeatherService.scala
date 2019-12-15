package service

import cats.effect.Sync
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType}
import repository.{GpsCoordinates, WeatherApi}

class WeatherService[F[_]: Sync](weatherApi: WeatherApi[F]) extends Http4sDsl[F] {

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(
        Stream.eval(
          weatherApi.get(GpsCoordinates.resorts.CrestedButte)
        ).map(_.asJson.noSpaces)
        ,
        `Content-Type`(MediaType.application.json)
      )
  }
}
