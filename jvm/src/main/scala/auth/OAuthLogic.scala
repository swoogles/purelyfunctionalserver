package auth

import org.http4s.{AuthScheme, Credentials, Request, UrlForm}
import cats.effect.IO
import cats.implicits._
import org.http4s.circe.jsonOf
import io.circe.generic.auto._
import org.http4s.headers.{Authorization, Cookie}
import org.http4s.client.Client
import org.http4s.Method._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.dsl.io._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Uri}
import zio.{DefaultRuntime, Runtime, Task, ZIO}
import zio.interop.catz._

case class TokenResponse(
  access_token: String,
  id_token: String,
  scope: String,
  expires_in: Int,
  token_type: String
)

case class UserInfo(sub: String)

class OAuthLogic(C: Client[Task]) extends Http4sClientDsl[Task] {
  private val domain = System.getenv("OAUTH_DOMAIN")
  private val clientId = System.getenv("OAUTH_CLIENT_ID")
  private val clientSecret = System.getenv("OAUTH_CLIENT_SECRET")
  private val callbackUrl = "https://purelyfunctionalserver.herokuapp.com/oauth/callback" // TODO make this a property or something
  private val parameterisedUri = "https://quiet-glitter-8635.auth0.com/oauth/token" // TODO Use  Oauth domain

  private val chaoticPublicUser = "ChaoticPublicUser"

  implicit def userInfoDecoder: EntityDecoder[Task, UserInfo] =
    jsonOf

  def getUserInfo(accessToken: String) = {
    println("About to retrieve userInfo for token: " + accessToken)
    C.expect[UserInfo](
        GET(
          Uri.fromString("https://quiet-glitter-8635.auth0.com/userinfo").right.get,
          Authorization(Credentials.Token(AuthScheme.Bearer, accessToken))
        )
      )
      .map { userInfoResponse: UserInfo =>
        println("UserInfoResponse: " + userInfoResponse); userInfoResponse
      }
      .catchAll(error => {
        println("error getting user info: " + error)
        ZIO { throw new RuntimeException(error) }
      })
//      .handleErrorWith( error => IO { println("user info request error : " + error); error.getMessage})

  }

  def getTokenFromCallbackCode(code: String) = {

    val postRequest: Task[Request[Task]] = POST[UrlForm](
      UrlForm(
        "grant_type"    -> "authorization_code",
        "client_id"     -> clientId,
        "client_secret" -> clientSecret,
        "redirect_uri"  -> callbackUrl,
        "code"          -> code
      ),
      Uri.fromString("https://quiet-glitter-8635.auth0.com/oauth/token").right.get
    )

    implicit def commitEntityDecoder: EntityDecoder[Task, TokenResponse] =
      jsonOf

    C.expect[TokenResponse](postRequest)
      .map { response =>
        println("Response from token call: " + response)
        response
      }
  }

  def getTokenFromRequest(request: Request[Task]) = {
    request.headers.foreach(
      header => println("Header  name: " + header.name + "  value: " + header.value)
    )
    val tokenFromAuthorizationHeaderAttempt =
      request.headers.get(CaseInsensitiveString("Authorization"))
    tokenFromAuthorizationHeaderAttempt
      .map(header => header.value)
      .map(_.split("\\s+")(1)) // This is how I turn "Bearer yz3423..." into just the value "yz3423..."
      .orElse {
        println("Couldn't get token from Authorization header. Looking at queryParameters now")
        val queryParamResult = request.params.get("access_token")
        queryParamResult
      }
  }

  implicit val runtime: Runtime[Any] = new DefaultRuntime {}

  def getUserFromRequest(request: Request[Task]): Sub =
    getTokenFromRequest(request)
      .map(
        token =>
          runtime.unsafeRun(
            getUserInfo(token)
              .fold(failure =>
                      throw new RuntimeException("getUserInfo Failure: " + failure.getMessage),
                    userInfo => Sub(userInfo.sub))
          )
      )
      .getOrElse(Sub(chaoticPublicUser))

  def getOptionalUserFromRequest(request: Request[Task]): Option[Sub] =
    getTokenFromRequest(request)
      .map(token => {
        runtime.unsafeRun(
          getUserInfo(token).fold(failure => throw new RuntimeException("ack!"),
                                  userInfo => Sub(userInfo.sub))
        )
      })

}