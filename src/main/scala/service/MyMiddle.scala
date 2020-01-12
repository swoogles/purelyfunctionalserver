package service

import cats.data.NonEmptyList
import cats.effect.IO
import org.http4s.headers.Cookie
import org.http4s.{Header, HttpRoutes, RequestCookie, Response, Status}

object MyMiddle {
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
  def apply(service: HttpRoutes[IO], header: Header) =
    service.map(addHeader(_, header)).map(addCookie)
}
