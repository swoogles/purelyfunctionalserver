package service

import cats.effect.IO
import service.AuthBackingStores.User
import tsec.authentication.{
  BackingStore,
  BearerTokenAuthenticator,
  SecuredRequestHandler,
  TSecAuthService,
  TSecBearerToken,
  TSecTokenSettings
}
import tsec.common.SecureRandomId
import zio.Task
import zio.interop.catz._

import scala.concurrent.duration._

class AuthenticationBackends(
  val bearerTokenStore: BackingStore[Task, SecureRandomId, TSecBearerToken[Int]],
  val userStore: BackingStore[Task, Int, User]
) {

  type AuthService = TSecAuthService[User, TSecBearerToken[Int], Task]

  private val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes, //Absolute expiration time
    maxIdle = None
  )

  private val bearerTokenAuth: BearerTokenAuthenticator[Task, Int, User] =
    BearerTokenAuthenticator(
      bearerTokenStore,
      userStore,
      settings
    )

  val Auth: SecuredRequestHandler[Task, Int, User, TSecBearerToken[Int]] =
    SecuredRequestHandler(bearerTokenAuth)
}
