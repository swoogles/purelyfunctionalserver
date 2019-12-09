package repository

import java.time.format.DateTimeParseException

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, EntityEncoder, Uri}
import cats.syntax.either._
// import cats.syntax.either._

import io.circe.{ Decoder, Encoder }
// import io.circe.{Decoder, Encoder}

import java.time.Instant
// import java.time.Instant


trait Github[F[_]]{
  def get(userName: String, repoName: String): F[Github.Tree]
}

object Github {
  def apply[F[_]](implicit ev: Github[F]): Github[F] = ev

  final case class Author(name: String, email: String, date: Instant)

  object Author {
    implicit val encodeInstant: Encoder[Instant] = Encoder.encodeString.contramap[Instant](_.toString)
    // encodeInstant: io.circe.Encoder[java.time.Instant] = io.circe.Encoder$$anon$1@661d3b8e

    implicit val decodeInstant: Decoder[Instant] = Decoder.decodeString.emap { str =>
//      Either.catchNonFatal(Instant.parse(str)).leftMap(t => "Instant")
      try {
        Right(Instant.parse(str))
      } catch {case ex: DateTimeParseException => Left("Failed to parse: " + str)}
    }
  }
  final case class Commit(author: Author)
  final case class Tree(sha: String, commit: Commit)
  object Tree {

    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, Tree] =
      jsonOf
    implicit def commitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Tree] =
      jsonEncoderOf

  }

  final case class GithubError(e: Throwable) extends RuntimeException

  def impl[F[_]: Sync](C: Client[F]): Github[F] = new Github[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._

    def get(userName: String, repoName: String): F[Github.Tree] = {
      val parameterisedUri = s"https://api.github.com/repos/$userName/$repoName/commits/master"
      C.expect[Github.Tree](GET(Uri.unsafeFromString(parameterisedUri)))
        .adaptError{ case t => GithubError(t)} // Prevent Client Json Decoding Failure Leaking
    }
  }
}
