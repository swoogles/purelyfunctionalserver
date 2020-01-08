package service

import cats.effect.IO
import service.AuthBackingStores.User
import tsec.authentication.{BackingStore, BearerTokenAuthenticator, SecuredRequestHandler, TSecAuthService, TSecBearerToken, TSecTokenSettings}
import tsec.common.SecureRandomId
import scala.concurrent.duration._

class AuthenticationBackends (
                             val bearerTokenStore: BackingStore[IO, SecureRandomId, TSecBearerToken[Int]],
                             val userStore: BackingStore[IO, Int, User]
                           ) {

  type AuthService = TSecAuthService[User, TSecBearerToken[Int], IO]

  private val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes, //Absolute expiration time
    maxIdle = None
  )

  private val bearerTokenAuth: BearerTokenAuthenticator[IO, Int, User] =
    BearerTokenAuthenticator(
      bearerTokenStore,
      userStore,
      settings
    )


  val Auth: SecuredRequestHandler[IO, Int, User, TSecBearerToken[Int]] =
    SecuredRequestHandler(bearerTokenAuth)
}
