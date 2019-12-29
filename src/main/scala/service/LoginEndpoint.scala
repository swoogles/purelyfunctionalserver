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
  val service = HttpRoutes.of[IO] {
    case GET -> Root / userId => {
      println(s"Attempting to login userId=$userId")
      val newUserResult = userStore.put(User(idInt = userId.toInt, name = s"name_$userId", age = 1)).unsafeRunSync()
      val storedUser: Option[User] = userStore.get(userId.toInt).value.unsafeRunSync()
      storedUser match {
        case Some(storedUser) => println("Successfully stored user: " + storedUser)
        case None => throw new RuntimeException("Failed to insert!!")
      }
      val newBearerToken =
      TSecBearerToken[Int](
//        SecureRandomId.coerce(newUserResult.name),
        SecureRandomId.coerce(newUserResult.idInt.toString),
        newUserResult.idInt,
        Instant.now().plusSeconds(600), // TODO BAD SIDE EFFECT
        None
      )
      val storedToken = bearerTokenStore.put(newBearerToken).unsafeRunSync()
      Ok(
        storedToken.toString
      )
//      Ok(s"Should login userId=$userId")
    }
  }

}
