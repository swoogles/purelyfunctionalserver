package repository

import cats.Applicative
import cats.effect.{IO, Sync}
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, EntityEncoder, Uri}
import java.time.Instant

trait Github {
  def get(userName: String, repoName: String): IO[Github.Tree]

  def getUsersRecentActivity(userName: String): IO[List[Github.RepoActivity]]
}

object Github {
  final case class Author(name: String, email: String, date: Option[Instant] = None)

  final case class Commit(author: Author, message: String)
  final case class Tree(sha: String, commit: Commit, html_url: String)
  object Tree {

    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, Tree] =
      jsonOf
    implicit def commitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Tree] =
      jsonEncoderOf

  }


  final case class Repo(name: String)
  final case class Payload(commits: Option[List[Commit]])
  final case class UserActivityEvent(repo: Repo, payload: Payload, created_at: Instant)
  object  UserActivityEvent{

    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, UserActivityEvent] =
      jsonOf
    implicit def commitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, UserActivityEvent] =
      jsonEncoderOf

    implicit def listCommitEntityDecoder[F[_]: Sync]: EntityDecoder[F, List[UserActivityEvent]] =
      jsonOf
    implicit def listCommitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, List[UserActivityEvent]] =
      jsonEncoderOf
  }

  final case class RepoActivity(repo: Repo, commits: List[Commit])
  object  RepoActivity{

    implicit def commitEntityDecoder[F[_]: Sync]: EntityDecoder[F, RepoActivity] =
      jsonOf
    implicit def commitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, RepoActivity] =
      jsonEncoderOf

    implicit def listCommitEntityDecoder[F[_]: Sync]: EntityDecoder[F, List[RepoActivity]] =
      jsonOf
    implicit def listCommitEntityEncoder[F[_]: Applicative]: EntityEncoder[F, List[RepoActivity]] =
      jsonEncoderOf
  }

  final case class GithubError(e: Throwable) extends RuntimeException

  def impl(C: Client[IO]): Github = new Github{
    val dsl = new Http4sClientDsl[IO]{}
    import dsl._

    def get(userName: String, repoName: String): IO[Github.Tree] = {
      val parameterisedUri = s"https://api.github.com/repos/$userName/$repoName/commits/master"
      C.expect[Github.Tree](GET(Uri.unsafeFromString(parameterisedUri)))
        .adaptError{ case t => GithubError(t)} // Prevent Client Json Decoding Failure Leaking
    }

    def getUsersRecentActivity(userName: String): IO[List[Github.RepoActivity]] = {
      val parameterisedUri = s"https://api.github.com/users/$userName/events/public"
      C.expect[List[Github.UserActivityEvent]](GET(Uri.unsafeFromString(parameterisedUri)))
        .map{ userActivityEvents =>
          val repoActivity: Map[Repo, List[UserActivityEvent]] = userActivityEvents.groupBy( _.repo )
          val mapResults: Map[Repo, List[Commit]] =
            repoActivity.map {
              case (repo, groupedEvents) => (repo, groupedEvents.flatMap(_.payload.commits).flatten)
            }
          val result: List[RepoActivity] =
            mapResults.map{
              case (repo, commits) => RepoActivity(repo, commits)
            }.filter(_.commits.nonEmpty)
              .toList
          result
          }
        .adaptError{ case t =>
          println("error:" + t)
          GithubError(t)} // Prevent Client Json Decoding Failure Leaking
    }
  }
}
