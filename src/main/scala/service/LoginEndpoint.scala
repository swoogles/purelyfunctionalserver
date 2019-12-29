package service

import java.time.Instant

import cats.data.OptionT
import cats.effect.{IO, Sync}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import repository.TodoRepository
import service.AuthHelpers.User
import service.AuthenticatedEndpoint.defaultUser
import tsec.authentication.{BackingStore, TSecBearerToken}
import tsec.common.SecureRandomId

class LoginEndpoint [F[_]: Sync](userStore: BackingStore[IO, Int, User], bearerTokenStore: BackingStore[IO, SecureRandomId, TSecBearerToken[Int]]) extends Http4sDsl[IO] {
  case class RequestWithToken(token: String)
  def contactOAuthProvider(userId: Int): RequestWithToken = {
    RequestWithToken(s"Fresh token for $userId")
  }
  def generateNewTokenFor(user: User) = {
    TSecBearerToken[Int](
      //        SecureRandomId.coerce(newUserResult.name),
      SecureRandomId.coerce(user.idInt.toString),
      user.idInt,
      Instant.now().plusSeconds(600), // TODO BAD SIDE EFFECT
      None
    )
  }
  val service = HttpRoutes.of[IO] {
    case GET -> Root / userId => {
      println(s"Attempting to login userId=$userId")
      val result: String = userStore.get(userId.toInt).value.unsafeRunSync() match {
        case None => {
          val newUserResult = userStore.put(User(idInt = userId.toInt, name = s"name_$userId", age = 1)).unsafeRunSync()
          val storedUser: User = userStore.get(userId.toInt).value.unsafeRunSync().get
          val newBearerToken = generateNewTokenFor(storedUser)
          val storedToken = bearerTokenStore.put(newBearerToken).unsafeRunSync()
          storedToken.toString
        }
        case Some(existingUser) => {
          val result = bearerTokenStore.get(SecureRandomId.coerce(existingUser.idInt.toString)).map {
            existingToken => s"User exists with token $existingToken"
          }.getOrElseF {
            val newBearerToken = generateNewTokenFor(existingUser)
            println(s"Generating a new token: $newBearerToken")
            bearerTokenStore.put(newBearerToken)
          }
          result.unsafeRunSync().toString
        }
      }
      Ok(result)
    }
  }

}
