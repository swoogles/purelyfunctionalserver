package patient_settings

import org.http4s.headers.{`Content-Type`, Location}
import auth.AuthLogic
import org.http4s.{HttpRoutes, MediaType}
import org.http4s.dsl.Http4sDsl
import zio.Task
import zio.interop.catz._
import io.circe.generic.auto._
import io.circe.syntax._

import auth.AuthLogic
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Type`, Location}
import org.http4s.{HttpRoutes, MediaType, Uri}
import zio.Task
import zio.interop.catz._

class SettingsService(
  settingsLogic: SettingsLogic,
  authLogic: AuthLogic
) extends Http4sDsl[Task] {

  val service: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case request @ GET -> Root / settingName => {
      val user = authLogic.getUserFromRequest(request)
      Ok(
        settingsLogic
          .getValueFor(Preference(settingName), user)
          .getOrElse(
            settingsLogic
              .getDefaultValueFor__unsafe(Preference(settingName))
              .forUser(user)
          )
          .asJson
          .noSpaces,
        `Content-Type`(MediaType.application.json)
      )
    }

    case req @ POST -> Root => {
      val user = authLogic.getUserFromRequest(req)
      println("Authenticated user: " + user)
      for {
        userSetting: UserSettingWithValue <- req.decodeJson[UserSettingWithValue]
        result <- Created(
          settingsLogic.saveValue(userSetting).asJson.noSpaces,
          Location(Uri.unsafeFromString(s"/user_settings/${userSetting.preference.name}"))
        )
      } yield {
        result
      }
    }
  }
}
