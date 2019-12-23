package service

import cats.effect.Sync
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType}
import repository.Github

class OAuthService[F[_]: Sync]() extends Http4sDsl[F] {

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "callback"  =>
      Ok(
        "Yo bro. You authenticated"
        , `Content-Type`(MediaType.text.plain))

    case GET -> Root / "logout"  =>
      Ok(
        "Yo bro. You logged out"
        , `Content-Type`(MediaType.text.plain))
  }

}
