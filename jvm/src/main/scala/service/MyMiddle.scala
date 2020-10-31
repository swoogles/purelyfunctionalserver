package service

import auth.AuthLogic
import cats.data.{Kleisli, NonEmptyList, OptionT}
import cats.effect.{ConcurrentEffect, IO}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Cookie, Location}
import org.http4s.{Header, HttpRoutes, Request, RequestCookie, Response, Status, Uri}
import zio.{DefaultRuntime, Runtime, Task}
import zio.interop.catz._

class MyMiddle(
  authLogic: AuthLogic
) extends Http4sDsl[Task] {
  val uglyRuntime: Runtime[Any] = new DefaultRuntime {}

  def addHeader(resp: Response[Task], header: Header) =
    resp match {
      case Status.Successful(resp) => resp.putHeaders(header)
      case resp                    => resp
    }

  def addCookie(resp: Response[Task]) =
    resp match {
      case Status.Successful(resp) => {
        resp.addCookie("access_token", "NEED_A_REAL_TOKEN_VALUE")
      }
      case resp => resp
    }

  //  Cookie(NonEmptyList[RequestCookie](RequestCookie("name", "cookieValue"), List()))
//  def apply(service: HttpRoutes[Task], header: Header): HttpRoutes[Task] =
//    service.map(addHeader(_, header)).map(addCookie)

//  val authUser: Kleisli[OptionT[Task, Exception], Request[Task], Sub] =
//    authLogic.getOptionalUserFromRequest _
//    Kleisli(_ => OptionT.liftF(Task(???)))

  object AccessTokenParamMatcher extends QueryParamDecoderMatcher[String]("access_token")

  def applyBeforeLogic(service: HttpRoutes[Task]) =
    HttpRoutes.of[Task] {
      // pf: PartialFunction[Request[F], F[Response[F]]]
      case request @ GET -> Root / "html" / "PhysicalTherapyTracker" / "index.html" :? AccessTokenParamMatcher(
            accessToken
          ) => {
        println("Before actual resource behavior")
        authLogic.getOptionalUserFromRequest(request) match {
          case Some(user) => {
            println("user from accessToken: " + user)
            val result: Task[Response[Task]] = service.apply(request).value.map {
              case Some(response: Response[Task]) => {
                println("Got a response from the underlying service: " + response)
                response.withStatus(Ok)
              }
              case None => uglyRuntime.unsafeRun(NotFound("Dunno what to do with you."))
            }
            result
          }
          case None => {
            println("No access token. Need to login immediately.")
            throw new RuntimeException("Shouldn't actually hit this!")
            PermanentRedirect(
              Location(
                Uri.fromString("https://purelyfunctionalserver.herokuapp.com/oauth/login").right.get
              )
            )
          }
        }
      }
      // TODO de-duplicate logic
      case request @ GET -> Root / "html" / "PhysicalTherapyTracker" / "index.html" => {
        println("Before actual resource behavior")
        authLogic.getOptionalUserFromRequest(request) match {
          case Some(user) => {
            println("user from accessToken: " + user)
            val result: Task[Response[Task]] = service.apply(request).value.map {
              case Some(response: Response[Task]) => {
                println("Got a response from the underlying service: " + response)
                response.withStatus(Ok)
              }
              case None => uglyRuntime.unsafeRun(NotFound("Dunno what to do with you."))
            }
            result
          }
          case None => {
            println("No access token. Need to login immediately.")
            PermanentRedirect(
              Location(
                Uri.fromString("https://purelyfunctionalserver.herokuapp.com/oauth/login").right.get
              )
            )
          }
        }
      }
      case request => {
        val result: Task[Response[Task]] = service.apply(request).value.map {
          case Some(response: Response[Task]) => {
            println("Got a response from the underlying service: " + response)
            response.withStatus(Ok)
          }
          case None => uglyRuntime.unsafeRun(NotFound("Dunno what to do with you."))
        }
        result

      }
    }

}
