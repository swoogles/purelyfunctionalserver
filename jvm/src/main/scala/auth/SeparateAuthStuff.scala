package auth

import cats.effect.{ConcurrentEffect, IO, Sync}
import com.billding.settings.Sub
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response}

case class AuthorizedRequest[F[_]](sub: String, request: Request[F])

case class AccessToken(value: String)

class SeparateAuthStuff[F[_]: ConcurrentEffect]()(implicit f: Sync[F]) extends Http4sDsl[IO] {

  val PublicChaoticUser = Sub("PUBLIC_CHAOTIC_USER")
  case class AuthorizedRequest[G[_]](sub: Sub, request: Request[G])

  def getFederatedSubFromAccessToken(accessToken: AccessToken): Option[Sub] =
    ???

  def getAccessToken[F[_]](request: Request[F]): Option[AccessToken] =
    request.params
      .get("access_token")
      .map(accessToken => AccessToken(accessToken))

  /*
  val getUser: Request[F] => F[Sub] =
    request =>
      getAccessToken(request) match {
        case Some(accessToken) => f.defer( getFederatedSubFromAccessToken(accessToken).get)
        case None => throw new RuntimeException("Not handled!")
      }

   */

  def extractAuthParametersAndTurnIntoTypedRequest[G[_]: ConcurrentEffect](
    request: Request[G]
  ): Option[AuthorizedRequest[G]] =
    getAccessToken(request) match {
      case Some(accessToken) =>
        getFederatedSubFromAccessToken(accessToken) match {
          case Some(sub) => Some(AuthorizedRequest(sub, request))
          case None      => None
        }
      case None => None
    }

  def createAuthenticatedRequest[G[_]: ConcurrentEffect](
    request: Request[G],
    pf: PartialFunction[AuthorizedRequest[G], G[Response[G]]]
  ): Request[G] => G[Response[G]] = { request: Request[G] =>
    extractAuthParametersAndTurnIntoTypedRequest(request) match {
      case Some(authorizedRequest) => pf(authorizedRequest)
      case None                    => pf(AuthorizedRequest(PublicChaoticUser, request))
    }
  }

}
