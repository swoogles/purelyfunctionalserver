package auth

import cats.data.NonEmptyList
import io.chrisdavenport.vault.{Key, Vault}
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Type`, Authorization, Cookie, Location}
import org.http4s.{HttpRoutes, MediaType, Response, Uri, _}
import zio.interop.catz._
import zio.{DefaultRuntime, Runtime, Task}

case class OauthConfig(domain: String, clientId: String, clientSecret: String)

/*
"""
{"access_token":"zS3jo79RvUDNJ83G4wVOBVyorKhtQnID",
"id_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5qVkZOemMzTlRNNU9UWTBOak0yTnpZeVJFVTBSak5ETkRVelF6Y3lNMEZHT0RneE1ESkdOZyJ9.eyJpc3MiOiJodHRwczovL3F1aWV0LWdsaXR0ZXItODYzNS5hdXRoMC5jb20vIiwic3ViIjoiYXV0aDB8NWUxMTFiZWI5YzAyN2EwZTllNDZhZGVlIiwiYXVkIjoiRUFsSG1GdTN6QWtQUHF4ekpYVGhuTFhKUHl4OXFSZ1MiLCJpYXQiOjE1NzgxOTA3ODMsImV4cCI6MTU3ODE5MDk2M30.m-hAsqcVzghuOFvNnsjF3X5VR-_GCgzXBIJ_KEFk4xg65lbK5hk_Z_BXNv7L1ysBkd3YHzSDvCfXl00xrz9Hm9oBpDVN9NCLKtDChCiH8IJ2IRIhlVRX69Ta6SusB1kGb9qAeMG3HI3YCacpgWKMFGhxe1vYlIRoRa_gxWhfH3w3aX7JJ5M69Ikf0NLHVn_cwBPAEQQ7oQoZU7m9Amj0iKx-WGXKMuqCQmJTZP2lfVHylltQvEoWKC04LBQB0fUPFnBjvt6pbVq3LAZKynnwuCO4DAvlAwWqRpuXL1yM3hlbd3mqOrtuXlEJdq3-aZpi_MHBOZnyKWnCckdFt5NDjQ",
"scope":"openid",
"expires_in":86400,
"token_type":"Bearer"
}
"""

 */

class OAuthService(C: Client[Task], authLogic: AuthLogic) extends Http4sDsl[Task] {
  // todo can I stop retrieving these in mock mode?
  val domain = System.getenv("OAUTH_DOMAIN")
  val clientId = System.getenv("OAUTH_CLIENT_ID")
  val clientSecret = System.getenv("OAUTH_CLIENT_SECRET")
  val fullConfig = OauthConfig(domain, clientId, clientSecret)

  import com.auth0.AuthenticationController
  import com.auth0.jwk.{JwkProvider, JwkProviderBuilder}

  val jwkProvider: JwkProvider = new JwkProviderBuilder(domain).build

  val controller: AuthenticationController =
    AuthenticationController
      .newBuilder(
        domain,
        clientId,
        clientSecret
      )
      .withJwkProvider(jwkProvider)
      .build

  val newKey: Task[Key[String]] = Key.newKey[Task, String]

  implicit val uglyRuntime: Runtime[Any] = new DefaultRuntime {}

  val service: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case req @ GET -> Root / "login" => {
      val newKey: Task[Key[String]] = Key.newKey[Task, String]
      val keyUsage: Task[Task[Response[Task]]] = for {
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

    case req @ GET -> Root / "get_token" => {
      val keyUsage = uglyRuntime.unsafeRun(for {
        flatKey <- newKey
      } yield {
        println("OauthService.callback.flatKey: " + flatKey)
        req.attributes.insert(flatKey, "oauthtoken")
//        Ok(
//          Authorization(Credentials.Token(AuthScheme.Bearer, "some_token")),
//          Cookie(NonEmptyList[RequestCookie](RequestCookie("name", "cookieValue"), List()))
//        )
        flatKey
      })
      val vault = Vault.empty.insert(keyUsage, "secret token from vault!!")
      val retrievedValue: Option[String] = vault.lookup(keyUsage)
      println("key from vault: " + retrievedValue.get)

//      import org.http4s.dsl.io._, org.http4s.implicits._

      val uri = Uri
        .fromString("http://localhost:8080/resources/html/index.html?access_token=needARealToken")
        .right
        .get
      println("parsed uri with query param: " + uri)
      PermanentRedirect(
        Location(uri),
        Authorization(Credentials.Token(AuthScheme.Bearer, "SECRET TOKEN OF SUCCESS!"))
      )

    }

    case req @ GET -> Root / "check_token" =>
      val lookupKey = uglyRuntime.unsafeRun(for {
        flatKey <- newKey
      } yield {
        println("OauthService.callback.flatKey: " + flatKey)
        flatKey
      })

      println("*some* attribute exists: " + req.attributes.isEmpty)
      println("req.authType: " + req.authType)

      Ok(
        "Probably didn't have a token :(",
        Authorization(Credentials.Token(AuthScheme.Bearer, "some_token")),
        Cookie(NonEmptyList[RequestCookie](RequestCookie("name", "cookieValue"), List()))
      )

    case req @ GET -> Root / "callback" => {
      req.params.foreach(param => println("Req.param key: " + param._1 + "  value: " + param._2))
      val auth0code = req.params("code")

      println("req.attributes: " + req.attributes)
      val keyUsage: Task[Response[Task]] = uglyRuntime.unsafeRun(for {
        tokenResponse <- authLogic.getTokenFromCallbackCode(auth0code)
        userInfo      <- authLogic.getUserInfo(tokenResponse.access_token)
      } yield {
        println("UserInfo: " + userInfo)
        val uri = Uri
          .fromString(
            // TODO Figure out a less brittle solution here
            s"https://purelyfunctionalserver.herokuapp.com/resources/html/PhysicalTherapyTracker/index.html?access_token=${tokenResponse.access_token}"
          )
          .right
          .get
        PermanentRedirect(
          Location(uri),
          Authorization(Credentials.Token(AuthScheme.Bearer, tokenResponse.access_token)),
          Cookie(
            NonEmptyList[RequestCookie](RequestCookie("accessTokenFromCallback",
                                                      tokenResponse.access_token),
                                        List())
          )
        )
      })
      println("Key usage: " + keyUsage)

      keyUsage
    }

    case GET -> Root / "logout" =>
      Ok("Yo bro. You logged out", `Content-Type`(MediaType.text.plain))
  }

}
