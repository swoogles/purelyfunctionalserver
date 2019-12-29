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

  val bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer: BackingStore[IO, SecureRandomId, TSecBearerToken[Int]] =
    dummyBackingStore[IO, SecureRandomId, TSecBearerToken[Int]](tokenValue => {// This function is: Int => SecureRandomId
      println(s"Turning s.id: ${tokenValue.id} into a SecureRandomId: ${SecureRandomId.coerce(tokenValue.id) }")
      SecureRandomId.coerce(tokenValue.id) // TODO Restore as entire body of this function
    })

  //We create a way to store our users. You can attach this to say, your doobie accessor
  val userStoreThatShouldBeInstantiatedOnceByTheServer: BackingStore[IO, Int, User] =
    dummyBackingStore[IO, Int, User](
      getId = (user: User) =>  user.idInt
    ) //This function is: User => Int

  private val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes, //Absolute expiration time
    maxIdle = None
  )

  private val bearerTokenAuth: BearerTokenAuthenticator[IO, Int, User] =
    BearerTokenAuthenticator(
      bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer,
      userStoreThatShouldBeInstantiatedOnceByTheServer,
      settings
    )


  private val Auth: SecuredRequestHandler[IO, Int, User, TSecBearerToken[Int]] =
    SecuredRequestHandler(bearerTokenAuth)

  val authService1: AuthService = TSecAuthService {
    case request @ GET -> Root / "api" asAuthed user =>
      /*
      Note: The request is of type: SecuredRequest, which carries:
      1. The request
      2. The Authenticator (i.e token)
      3. The identity (i.e in this case, User)
       */

      val r: SecuredRequest[IO, User, TSecBearerToken[Int]] = request
      println(s"SecureRequest: $r")
      println("Authenticated User is: " + user)
      Ok("Super secure info")  // TODO Unsafe. Leaking the whole User

    case request @ GET -> Root / "logout" asAuthed user => {
      val r: SecuredRequest[IO, User, TSecBearerToken[Int]] = request
      Ok(
        bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer.delete(SecureRandomId.coerce(user.idInt.toString))
          .toString()
      )

    }
  }

  private val authedService2: AuthService = TSecAuthService {
    case GET -> Root / "api2" asAuthed user =>
      Ok()
  }

  val lifted: HttpRoutes[IO]         = Auth.liftService(authService1)
  private val liftedComposed: HttpRoutes[IO] = Auth.liftService(authService1 <+> authedService2)
}
