package service

import cats.effect.{ConcurrentEffect, IO, Sync}
import org.http4s.circe.jsonOf
import io.circe.generic.auto._
import repository.ForeCast
//import com.auth0.SessionUtils
import fs2.Stream
import io.chrisdavenport.vault.Key
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response, Uri}
import org.http4s._
import org.http4s.client.Client

case class OauthConfig(domain: String, clientId: String, clientSecret: String)

class OAuthLogic[F[_]: Sync](C: Client[IO]) {
  import org.http4s.Method._
  import org.http4s.{EntityDecoder, Uri}
  import org.http4s.client.dsl.io._

  val domain = System.getenv("OAUTH_DOMAIN")
  val clientId = System.getenv("OAUTH_CLIENT_ID")
  val clientSecret = System.getenv("OAUTH_CLIENT_SECRET")
  val callbackUrl = "https://purelyfunctionalserver.herokuapp.com/oauth/callback" // TODO make this a property or something
  val parameterisedUri = "https://quiet-glitter-8635.auth0.com/oauth/token"
  def doStuff(code: String) = {

    val postRequest: IO[Request[IO]] = POST[UrlForm](
      UrlForm(
        "grant_type" -> "authorization_code",
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "redirect_uri" -> callbackUrl,
        "code" -> code
      ),
      Uri.fromString("https://my-lovely-api.com/oauth2/token").right.get,
    )

    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, OauthConfig] =
      jsonOf
//    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, ForeCast] =
//      jsonOf

    C.expect[String](postRequest)
      .map(response => println("Response from token call: " + response))
      .handleErrorWith( error => IO { println("error : " + error)})
//    C.expect[String](postRequest)
      //        .map( forecastWithoutLocationName => forecastWithoutLocationName.copy(location = Some(gpsCoordinates.locationName)))
//      .adaptError { case t =>
//        println("error: " + t)
//        WeatherError(t) } // Prevent Client Json Decoding Failure Leaking
  }

}

class OAuthService[F[_]: ConcurrentEffect](C: Client[IO]) extends Http4sDsl[F] {
  val domain = System.getenv("OAUTH_DOMAIN")
  val clientId = System.getenv("OAUTH_CLIENT_ID")
  val clientSecret = System.getenv("OAUTH_CLIENT_SECRET")
  val fullConfig = OauthConfig(domain, clientId, clientSecret)

  import com.auth0.AuthenticationController
  import com.auth0.jwk.JwkProvider
  import com.auth0.jwk.JwkProviderBuilder

  val jwkProvider: JwkProvider = new JwkProviderBuilder(domain).build
  val controller: AuthenticationController =
    AuthenticationController.newBuilder(
      domain,
      clientId,
      clientSecret
    ).withJwkProvider(jwkProvider).build

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / "login"  => {
      val newKey: IO[Key[String]] = Key.newKey[IO, String]
      val keyUsage: IO[F[Response[F]]] = for {
        flatKey <- newKey
      } yield {
        println("OauthService.callback.flatKey: " + flatKey)
        req.attributes.insert(flatKey, "oauthtoken")
        Ok("Sure, you good")
      }

      val callbackUrl = "https://purelyfunctionalserver.herokuapp.com/oauth/callback" // TODO make this a property or something
      val authorizeUrl = controller.buildAuthorizeUrl(new ScalaHttpServletRequest(req), callbackUrl)

      PermanentRedirect(Location(Uri.fromString(authorizeUrl.build()).right.get)) // TODO Unsafe parsing

    }

    case req @ GET -> Root / "callback"  => {
      val authLogic = new OAuthLogic[IO](C)
      req.params.foreach( param => println("Req.param key: " + param._1 + "  value: " + param._2))
      val auth0code = req.params("code")

      println("req.attributes: " + req.attributes)
      val newKey: IO[Key[String]] = Key.newKey[IO, String]
      val keyUsage = (for {
        flatKey <- newKey
      _ <- authLogic.doStuff(auth0code)
      } yield {
        println("OauthService.callback.flatKey: " + flatKey)
        req.attributes.insert(flatKey, "oauthtoken")
        Ok("Sure, you good")
      }).unsafeRunSync()
      println("Key usage: " + keyUsage)

      Ok("We did some stuff!")
    }

    case GET -> Root / "logout"  =>
      Ok(
        "Yo bro. You logged out"
        , `Content-Type`(MediaType.text.plain))
  }

}
