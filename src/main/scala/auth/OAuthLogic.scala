package auth

import org.http4s.{AuthScheme, Credentials, Request, UrlForm}

import cats.effect.{IO, Sync}
import org.http4s.circe.jsonOf
import io.circe.generic.auto._
import org.http4s.headers.{Authorization, Cookie}
import org.http4s.client.Client
import org.http4s.Method._
import org.http4s.client.dsl.io._
import org.http4s.{EntityDecoder, Uri}

case class TokenResponse(
                          access_token: String,
                          id_token: String,
                          scope: String,
                          expires_in: Int,
                          token_type: String
                        )

case class UserInfo(sub: String)

class OAuthLogic[F[_]: Sync](C: Client[IO])
{
  val domain = System.getenv("OAUTH_DOMAIN")
  val clientId = System.getenv("OAUTH_CLIENT_ID")
  val clientSecret = System.getenv("OAUTH_CLIENT_SECRET")
  val callbackUrl = "https://purelyfunctionalserver.herokuapp.com/oauth/callback" // TODO make this a property or something
  val parameterisedUri = "https://quiet-glitter-8635.auth0.com/oauth/token" // TODO Use  Oauth domain

  implicit def userInfoDecoder[F[_]: Sync]: EntityDecoder[F, UserInfo] =
    jsonOf
  def getUserInfo(accessToken: String) = {
    println("About to retrieve userInfo for token: " + accessToken)
    C.expect[UserInfo](
      GET(
        Uri.fromString("https://quiet-glitter-8635.auth0.com/userinfo").right.get,
        Authorization(Credentials.Token(AuthScheme.Bearer, accessToken))
      )
    ).map{userInfoResponse: UserInfo => println("UserInfoResponse: " + userInfoResponse); userInfoResponse}
      .handleErrorWith( error => {
        println("error getting user info: " + error)
        IO {  throw new RuntimeException(error)}
      })
//      .handleErrorWith( error => IO { println("user info request error : " + error); error.getMessage})


  }
  def getTokenFromCallbackCode(code: String) = {

    val postRequest: IO[Request[IO]] = POST[UrlForm](
      UrlForm(
        "grant_type" -> "authorization_code",
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "redirect_uri" -> callbackUrl,
        "code" -> code
      ),
      Uri.fromString("https://quiet-glitter-8635.auth0.com/oauth/token").right.get,
    )

    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, TokenResponse] =
      jsonOf
//    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, ForeCast] =
//      jsonOf

    C.expect[TokenResponse](postRequest)
      .map{response =>
        println("Response from token call: " + response)
        response

      }
//      .handleErrorWith( error => IO { println("token request error : " + error)})
//    C.expect[String](postRequest)
      //        .map( forecastWithoutLocationName => forecastWithoutLocationName.copy(location = Some(gpsCoordinates.locationName)))
//      .adaptError { case t =>
//        println("error: " + t)
//        WeatherError(t) } // Prevent Client Json Decoding Failure Leaking
  }

}
