package service

import cats.effect.{IO, Sync}
import com.auth0.SessionUtils
import fs2.Stream
import io.chrisdavenport.vault.Key
import io.circe.generic.auto._
import io.circe.syntax._
import javax.servlet.http.{HttpServlet, HttpServletRequestWrapper}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response, Uri}
import repository.Github

case class OauthConfig(domain: String, clientId: String, clientSecret: String)

class OAuthService[F[_]: Sync]() extends Http4sDsl[F] {
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
    case req @ GET -> Root / "manualCallback"  => {
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

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / "callback"  => {
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

    case GET -> Root / "logout"  =>
      Ok(
        "Yo bro. You logged out"
        , `Content-Type`(MediaType.text.plain))
  }

}
