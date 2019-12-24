package service

import cats.effect.Sync
import com.auth0.SessionUtils
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import javax.servlet.http.{HttpServlet, HttpServletRequestWrapper}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
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
  val controller: AuthenticationController = AuthenticationController.newBuilder(domain, clientId, clientSecret).withJwkProvider(jwkProvider).build


  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / "callback"  => {
      println("Full Config")
      println(fullConfig)

      val callbackUrl = "https://purelyfunctionalserver.herokuapp.com/oauth/callback" // TODO make this a property or something
      val authorizeUrl = controller.buildAuthorizeUrl(new ScalaHttpServletRequest(req), callbackUrl)

//      getServletConfig().getServletContext()

      PermanentRedirect(Location(Uri.fromString(authorizeUrl.build()).right.get)) // TODO Unsafe parsing
//      Ok(
//        "Yo bro. You authenticated"
//        , `Content-Type`(MediaType.text.plain))
    }

    case GET -> Root / "logout"  =>
      Ok(
        "Yo bro. You logged out"
        , `Content-Type`(MediaType.text.plain))
  }

}
