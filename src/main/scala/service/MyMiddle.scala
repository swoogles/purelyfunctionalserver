package service

import auth.OAuthLogic
import cats.data.{Kleisli, NonEmptyList, OptionT}
import cats.effect.{ConcurrentEffect, IO}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Cookie, Location}
import org.http4s.{Header, HttpRoutes, Request, RequestCookie, Response, Status, Uri}

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

//  val authUser: Kleisli[OptionT[IO, Exception], Request[IO], Sub] =
//    authLogic.getOptionalUserFromRequest _
//    Kleisli(_ => OptionT.liftF(IO(???)))

  def applyBeforeLogic(service: HttpRoutes[IO]) = {
    HttpRoutes.of[IO] {
      // pf: PartialFunction[Request[F], F[Response[F]]]
      case request @ GET -> Root / "html" / "index.html"=> {
        println("Before actual resource behavior")
        authLogic.getOptionalUserFromRequest(request) match {
          case Some(user) => {
            println("user from accessToken: " + user)
            val result: IO[Response[IO]] = service.apply(request).value.map{
              case Some(response: Response[IO]) => {
                println("Got a response from the underlying service: " + response)
                response.withStatus(Ok)
              }
              case None => NotFound("Dunno what to do with you.").unsafeRunSync()
            }
            result
          }
          case None => {
            println("No access token. Need to login immediately.")
            PermanentRedirect(Location(Uri.fromString("https://purelyfunctionalserver.herokuapp.com/oauth/login").right.get))
          }
        }
      }
      case request => {
        val result: IO[Response[IO]] = service.apply(request).value.map{
          case Some(response: Response[IO]) => {
            println("Got a response from the underlying service: " + response)
            response.withStatus(Ok)
          }
          case None => NotFound("Dunno what to do with you.").unsafeRunSync()
        }
        result

      }
    }
  }

}
