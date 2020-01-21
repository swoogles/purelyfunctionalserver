package service

import java.time.Instant

import cats.effect.IO
import io.chrisdavenport.vault.Key
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import service.AuthBackingStores.User
import tsec.authentication.{BackingStore, TSecBearerToken}
import tsec.common.SecureRandomId
import zio.{DefaultRuntime, Runtime, Task}
import zio.interop.catz._

class LoginEndpoint (
                      userStore: BackingStore[Task, Int, User],
                      bearerTokenStore: BackingStore[Task, SecureRandomId, TSecBearerToken[Int]]
                    ) extends Http4sDsl[Task] {
  implicit val runtime: Runtime[Any] = new DefaultRuntime {}
  case class RequestWithToken(token: String)
  def contactOAuthProvider(userId: Int): RequestWithToken = {
    RequestWithToken(s"Fresh token for $userId")
  }
  def generateNewTokenFor(user: User) = {
    TSecBearerToken[Int](
      //        SecureRandomId.coerce(newUserResult.name),
      SecureRandomId.coerce(user.idInt.toString),
      user.idInt,
      Instant.now().plusSeconds(30), // TODO BAD SIDE EFFECT
      None
    )
  }
  def createUserWithBearerToken(userId: String) =
  userStore.put(User(idInt = userId.toInt, name = s"name_$userId", age = 1))
    .flatMap( newUserResult =>
      bearerTokenStore.put(generateNewTokenFor(newUserResult))
    )

  def createBearerTokenForExistingUserIfNeeded(existingUser: User) = {
    val secureUserId = SecureRandomId.coerce(existingUser.idInt.toString)
    bearerTokenStore.get(secureUserId).map {
      existingToken =>
        if (existingToken.expiry.isBefore(Instant.now())) { // TODO Bad side effect
          val newBearerToken = generateNewTokenFor(existingUser)
          bearerTokenStore.update(newBearerToken)
            .map(ignoredToken => s"User exists, but needed to replace an expired token")
        } else {
          Task.succeed(s"User exists with a valid token")
        }
    }.getOrElseF {
      val newBearerToken = generateNewTokenFor(existingUser)
      println(s"Generating a new token: $newBearerToken")
      bearerTokenStore.put(newBearerToken)
    }
  }
  def loginLogicWithUserCreation(userId: String) =
    runtime.unsafeRun(userStore.get(userId.toInt).value) match {
      case None => createUserWithBearerToken(userId)
      case Some(existingUser) => createBearerTokenForExistingUserIfNeeded(existingUser)}
  val service = HttpRoutes.of[Task] {
    case request @ GET -> Root / userId => {
      println(s"Attempting to login userId=$userId")
      val result = loginLogicWithUserCreation(userId)
      Ok(runtime.unsafeRun(result).toString)
    }
  }

}
