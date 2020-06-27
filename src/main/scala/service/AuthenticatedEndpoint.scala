package service

import cats.effect.IO
import cats.syntax.semigroupk._
import org.http4s.dsl.io._
import tsec.authentication._
import tsec.common.SecureRandomId
import AuthBackingStores.{Role, User}
import zio.Task
import zio.interop.catz._

/*
class AuthenticatedEndpoint(
                             bearerTokenStore: BackingStore[Task, SecureRandomId, TSecBearerToken[Int]]
                           ) {

  type AuthService = TSecAuthService[User, TSecBearerToken[Int], Task]

  val service: AuthService = TSecAuthService {
    case request @ GET -> Root / "api" asAuthed user =>
      /*
      Note: The request is of type: SecuredRequest, which carries:
      1. The request
      2. The Authenticator (i.e token)
      3. The identity (i.e in this case, User)
 */

      val r: SecuredRequest[Task, User, TSecBearerToken[Int]] = request
      println(s"SecureRequest: $r")
      println("Authenticated User is: " + user)
      Ok("Super secure info")  // TODO Unsafe. Leaking the whole User

    case request @ GET -> Root / "logout" asAuthed user => {
      val r: SecuredRequest[Task, User, TSecBearerToken[Int]] = request
      Ok(
        bearerTokenStore.delete(SecureRandomId.coerce(user.idInt.toString))
          .toString()
      )
    }
  }

  private val authedService2: AuthService = TSecAuthService {
    case GET -> Root / "api2" asAuthed user =>
      Ok()
  }

  private val composedService: AuthService = service <+> authedService2
}

 */
