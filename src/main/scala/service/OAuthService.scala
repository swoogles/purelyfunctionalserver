package service

import cats.effect.{ConcurrentEffect, IO, Sync}
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
import java.io.InputStream

import io.circe.Json
import org.http4s.circe._
import org.http4s._

case class OauthConfig(domain: String, clientId: String, clientSecret: String)

class OAuthService[F[_]: ConcurrentEffect]() extends Http4sDsl[F] {
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
      req.params.foreach( param => println("Req.param key: " + param._1 + "  value: " + param._2))
      val auth0code = req.params("code")
      val decodedCode = jwkProvider.get(auth0code)
      println("DecodedCode: " + decodedCode)
//      controller.handle()
//      controller.handle(req.)
//      val x: F[Json] = Stream.eval(req.as[Json]).compile.toVector.r
      println("req.attributes: " + req.attributes)
//      println("req.decoded body: " + req.decode[String])
      val x: Stream[F, String] = req.body.map(_.toChar).fold("")(_ + _)
      val newKey: IO[Key[String]] = Key.newKey[IO, String]
      val keyUsage = (for {
        flatKey <- newKey
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
