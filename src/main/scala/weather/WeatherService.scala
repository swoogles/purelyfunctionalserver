package weather

import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.MediaType
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import service.User
import tsec.authentication.{TSecAuthService, TSecBearerToken, _}
import zio.Task
import zio.interop.catz._

class WeatherService(weatherApi: WeatherApi) extends Http4sDsl[Task] {
  type AuthService = TSecAuthService[User, TSecBearerToken[Int], Task]

  val service: AuthService = TSecAuthService {
    case GET -> Root asAuthed user =>
      Ok(
        Stream
          .eval(
            weatherApi.get(GpsCoordinates.resorts.CrestedButte)
          )
          .map(_.asJson.noSpaces)
          .handleErrorWith(
            error =>
              Stream.eval(Task.succeed {
                println(s"error: $error"); """{"error": "Couldn't find weather info" } """
              })
          ),
        `Content-Type`(MediaType.application.json)
      )
  }
}
