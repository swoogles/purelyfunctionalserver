package patient_settings

import auth.AuthLogic
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
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
          .getOrElse()
          .getExerciseHistoriesFor(exerciseName, user.id)
          .map(_.asJson.noSpaces)
          .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )
    }
    ???
  }
}
