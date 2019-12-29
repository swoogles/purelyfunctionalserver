package service

import java.time.Instant

import cats.effect.IO
import cats.syntax.semigroupk._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import tsec.authentication._
import tsec.common.SecureRandomId

import scala.concurrent.duration._
import AuthHelpers.{Role, User, dummyBackingStore}

object AuthenticatedEndpoint {

  type AuthService = TSecAuthService[User, TSecBearerToken[Int], IO]

  private val defaultUser =
    User(idInt = -1, age = 1, name = "default_name", Role.Administrator)

  private val defaultBearerToken =
    TSecBearerToken[Int](
      SecureRandomId.coerce(defaultUser.name),
      identity = 0,
      Instant.now().plusSeconds(60), // TODO BAD SIDE EFFECT
      None
    )

  private val bearerTokenStore: BackingStore[IO, SecureRandomId, TSecBearerToken[Int]] =
    dummyBackingStore[IO, SecureRandomId, TSecBearerToken[Int]](s => {// This function is: Int => SecureRandomId
      println(s"Turning s.id: ${s.id} into a SecureRandomId: ${SecureRandomId.coerce(s.id) }")
      SecureRandomId.coerce(s.id) // TODO Restore as entire body of this function
    }, () => defaultBearerToken)

  //We create a way to store our users. You can attach this to say, your doobie accessor
  private val userStore: BackingStore[IO, Int, User] =
    dummyBackingStore[IO, Int, User](
      getId = (user: User) =>  user.idInt
      , () => defaultUser
    ) //This function is: User => Int

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


  private val Auth: SecuredRequestHandler[IO, Int, User, TSecBearerToken[Int]] =
    SecuredRequestHandler(bearerTokenAuth)

  val authService1: AuthService = TSecAuthService {
    //Where user is the case class User above
    case request @ GET -> Root / "api" asAuthed user =>
      /*
      Note: The request is of type: SecuredRequest, which carries:
      1. The request
      2. The Authenticator (i.e token)
      3. The identity (i.e in this case, User)
       */

      val r: SecuredRequest[IO, User, TSecBearerToken[Int]] = request
      println("Authenticated User is: " + user)
      Ok(user.toString)  // TODO Unsafe. Leaking the whole User
  }

  private val authedService2: AuthService = TSecAuthService {
    case GET -> Root / "api2" asAuthed user =>
      Ok()
  }

  val lifted: HttpRoutes[IO]         = Auth.liftService(authService1)
  private val liftedComposed: HttpRoutes[IO] = Auth.liftService(authService1 <+> authedService2)
}
