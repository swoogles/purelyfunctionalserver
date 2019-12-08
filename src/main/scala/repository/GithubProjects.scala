package repository

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder}

trait Github[F[_]]{
  def get: F[Github.Commit]
}

object Github {
  def apply[F[_]](implicit ev: Github[F]): Github[F] = ev

  final case class Commit(sha: String) extends AnyVal
  object Commit {
    implicit val commitDecoder: Decoder[Commit] = deriveDecoder[Commit]
    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, Commit] =
      jsonOf
    implicit val commitEncoder: Encoder[Commit] = deriveEncoder[Commit]
    implicit def commitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Commit] =
      jsonEncoderOf
  }

  final case class GithubError(e: Throwable) extends RuntimeException

  def impl[F[_]: Sync](C: Client[F]): Github[F] = new Github[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._
    def get: F[Github.Commit] = {
      C.expect[Github.Commit](GET(uri"https://api.github.com/repos/swoogles/TrafficSimulation/commits/master"))
        .adaptError{ case t => GithubError(t)} // Prevent Client Json Decoding Failure Leaking
    }
  }
}
