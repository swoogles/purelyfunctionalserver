package billding

import java.time.LocalDate

import com.billding.exercises.Exercise
import com.billding.settings.{Setting, SettingWithValue, UserSettingWithValue}
import exercises.DailyQuantizedExercise
import org.scalajs.dom.raw.Storage
import sttp.model.{Header, Uri}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

import io.circe.generic.auto._
import sttp.client.circe._

object ApiInteractions {
  import sttp.client._

  val exerciseUri: Uri = uri"${Meta.host}/exercises"
  val quadSetUri: Uri = uri"${Meta.host}/exercises/QuadSets"

  implicit val backend = FetchBackend()
  implicit val ec = global

  def getHistory(storage: Storage, exercise: Exercise): Future[List[DailyQuantizedExercise]] = {
    val historyUri: Uri = uri"${Meta.host}/exercises/${exercise.id}"

    val request = {
      if (storage
            .getItem("access_token_fromJS")
            .nonEmpty) { // We have a stored token. Use it for getting authorized info
        basicRequest
          .get(historyUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
      } else if (Meta.accessToken.isDefined) { // We have a queryParameter token. Use it for getting authorized info. Non-ideal.
        basicRequest
          .get(historyUri.param("access_token", Meta.accessToken.get))
          .header(Header.authorization("Bearer", Meta.accessToken.get))
      } else { // no token. Request information for public, chaotic user.
        basicRequest
          .get(historyUri)
      }
    }

    for {
      response: Response[Either[String, String]] <- request.send()
    } yield {
      response.body match {
        case Right(jsonBody) => {
          circe.deserializeJson[List[DailyQuantizedExercise]].apply(jsonBody) match {
            case Right(value) => {
              value
            }
            case Left(failure) => List()
          }
        }
        case Left(failure) => {
          List()
        }
      }
    }

  }

  def getUserSetting(setting: Setting): Future[UserSettingWithValue] = {
    val storage = org.scalajs.dom.window.localStorage

    val settingsUri: Uri = uri"${Meta.host}/user_settings/${setting.name}"
    val request =
      if (storage.getItem("access_token_fromJS").nonEmpty) {
        basicRequest
          .get(settingsUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
      } else if (Meta.accessToken.isDefined) {
        basicRequest
          .get(settingsUri.param("access_token", Meta.accessToken.get))
      } else {
        basicRequest
          .get(settingsUri)
      }

    for {
      response: Response[Either[String, String]] <- request.send()
    } yield {

      response.body match {
        case Right(jsonBody) => {
          circe.deserializeJson[UserSettingWithValue].apply(jsonBody) match {
            case Right(value) => {
              value
            }
            case Left(failure) => throw new RuntimeException("Parse failure: " + failure)
          }
        }
        case Left(failure) => {
          throw new RuntimeException("failure: " + failure)
        }
      }
    }
  }

  //todo accept storage as parameter
  def postUserSetting(setting: SettingWithValue): Future[Int] = {
    val storage = org.scalajs.dom.window.localStorage

    val settingsUri: Uri = uri"${Meta.host}/user_settings"
    val request =
      if (storage.getItem("access_token_fromJS").nonEmpty) {
        basicRequest
          .post(settingsUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
          .body(setting)
      } else if (Meta.accessToken.isDefined) {
        basicRequest
          .post(settingsUri.param("access_token", Meta.accessToken.get))
          .body(setting)
      } else {
        basicRequest
          .body(setting)
          .post(settingsUri)
      }

    for {
      response: Response[Either[String, String]] <- request.send()
    } yield {
      response.body match {
        case Right(jsonBody) => {
          jsonBody.toInt
        }
        case Left(failure) => {
          println("Failed to submit armstretches with error: " + failure)
          0
        }
      }
    }
  }

  //todo accept storage as parameter
  def postExerciseSession(count: Int, exerciseName: String): Future[Int] = {
    val localDate = Time.formattedLocalDate()
    val exercise =
      DailyQuantizedExercise(name = exerciseName, day = LocalDate.parse(localDate), count = count)

    val storage = org.scalajs.dom.window.localStorage

    val request =
      if (storage.getItem("access_token_fromJS").nonEmpty) {
        basicRequest
          .post(exerciseUri)
          .auth
          .bearer(storage.getItem("access_token_fromJS"))
          .body(exercise)
      } else if (Meta.accessToken.isDefined) {
        basicRequest
          .body(exercise)
          .post(exerciseUri.param("access_token", Meta.accessToken.get))
      } else {
        basicRequest
          .body(exercise)
          .post(exerciseUri)
      }

    for {
      response: Response[Either[String, String]] <- request.send()
    } yield {
      response.body match {
        case Right(jsonBody) => {
          jsonBody.toInt
        }
        case Left(failure) => {
          println("Failed to submit armstretches with error: " + failure)
          0
        }
      }
    }
  }
}
