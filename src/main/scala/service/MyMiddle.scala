package service

import auth.OAuthLogic
import cats.data.NonEmptyList
import cats.effect.{ConcurrentEffect, IO}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Cookie, Location}
import org.http4s.{Header, HttpRoutes, RequestCookie, Response, Status, Uri}

class MyMiddle[F[_]: ConcurrentEffect](
                                        authLogic: OAuthLogic[IO]
                                      ) extends Http4sDsl[IO] {
  def addHeader(resp: Response[IO], header: Header) =
    resp match {
      case Status.Successful(resp) => resp.putHeaders(header)
      case resp => resp
    }

  def addCookie(resp: Response[IO]) =
    resp match {
      case Status.Successful(resp) => {
        resp.addCookie("access_token", "NEED_A_REAL_TOKEN_VALUE")
      }
      case resp => resp
    }

  //  Cookie(NonEmptyList[RequestCookie](RequestCookie("name", "cookieValue"), List()))
  def apply(service: HttpRoutes[IO], header: Header): HttpRoutes[IO] =
    service.map(addHeader(_, header)).map(addCookie)

  def applyBeforeLogic(service: HttpRoutes[IO]) = {
    HttpRoutes.of[IO] {
      // pf: PartialFunction[Request[F], F[Response[F]]]
      case request @ GET -> Root => {
        authLogic.getOptionalUserFromRequest(request) match {
          case Some(user) => service.apply(request).getOrElseF(NotFound("Dunno what to do with you."))
          case None => PermanentRedirect(Location(Uri.fromString("https://purelyfunctionalserver.herokuapp.com/oauth/login").right.get))
        }
      }
    }
  }

}
