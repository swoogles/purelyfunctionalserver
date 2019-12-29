package service

import java.time.Instant

import cats.effect.{Effect, IO, Sync}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import service.AuthHelpers.User
import tsec.authentication.{BackingStore, TSecBearerToken}
import tsec.common.SecureRandomId

class LoginEndpoint [F[_]: Sync](
                                  userStore: BackingStore[IO, Int, User],
                                  bearerTokenStore: BackingStore[IO, SecureRandomId, TSecBearerToken[Int]]
                                )(
                                  implicit ev: Effect[F]
                                ) extends Http4sDsl[F] {
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
  val service = HttpRoutes.of[F] {
    case GET -> Root / userId => {
      println(s"Attempting to login userId=$userId")
      val result = userStore.get(userId.toInt).value.unsafeRunSync() match {
        case None =>
          userStore.put(User(idInt = userId.toInt, name = s"name_$userId", age = 1))
            .flatMap( newUserResult => {
              val newBearerToken = generateNewTokenFor(newUserResult)
              bearerTokenStore.put(newBearerToken)
            })

        case Some(existingUser) => {
          val secureUserId = SecureRandomId.coerce(existingUser.idInt.toString)
            bearerTokenStore.get(secureUserId).map {
              existingToken =>
              if (existingToken.expiry.isBefore(Instant.now())) {// TODO Bad side effect
                val newBearerToken = generateNewTokenFor(existingUser)
                bearerTokenStore.update(newBearerToken)
                  .map(ignoredToken => s"User exists, but needed to replace an expired token")
              } else {
                ev.pure(s"User exists with a valid token")
              }
            }.getOrElseF {
              val newBearerToken = generateNewTokenFor(existingUser)
              println(s"Generating a new token: $newBearerToken")
              bearerTokenStore.put(newBearerToken)
            }
        }
      }
      Ok(result.unsafeRunSync().toString)
    }
  }

}
