package service

import java.time.Instant

import cats.effect.{IO, Sync}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import service.AuthHelpers.User
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
      Instant.now().plusSeconds(30), // TODO BAD SIDE EFFECT
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
          val secureUserId = SecureRandomId.coerce(existingUser.idInt.toString)
          val result =
            bearerTokenStore.get(secureUserId).map {
              existingToken =>
              if (existingToken.expiry.isBefore(Instant.now())) {// TODO Bad side effect
                val newBearerToken = generateNewTokenFor(existingUser)
                bearerTokenStore.update(newBearerToken).unsafeRunSync()// TODO Use this in a composed way
                s"User exists, but needed to replace an expired token"
              } else {
                s"User exists with a valid token"
              }
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
