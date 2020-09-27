package daml

import auth.AuthLogic
import fs2.Stream
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import zio.Task
import zio.interop.catz._

class DamlService(
  templateLogic: TemplateLogic,
  authLogic: AuthLogic
) extends Http4sDsl[Task] {

  val service: HttpRoutes[Task] = HttpRoutes.of[Task] {
    // pf: PartialFunction[Request[F], F[Response[F]]]
//    case request @ GET -> Root => {
//      val user = authLogic.getUserFromRequest(request)
//      Ok(
//        Stream("[") ++
//        exerciseLogic
//          .getExerciseHistoriesFor("QuadSets", user.id)
//          .map(_.asJson.noSpaces)
//          .intersperse(",") ++ Stream("]"),
//        `Content-Type`(MediaType.application.json)
//      )
//    }
//
//    case request @ GET -> Root / exerciseName => {
//      val user = authLogic.getUserFromRequest(request)
//      Ok(
//        Stream("[") ++
//          exerciseLogic
//            .getExerciseHistoriesFor(exerciseName, user.id)
//            .map(_.asJson.noSpaces)
//            .intersperse(",") ++ Stream("]"),
//        `Content-Type`(MediaType.application.json)
//      )
//    }

    case req @ POST -> Root => {
      val user = authLogic.getUserFromRequest(req)
      for {
        newExercise       <- req.decodeJson[Template[String]]
        wrappedResult <- templateLogic.insert(newExercise).map(_=>Created("Maybe we created a table!"))
        bigResult <- wrappedResult
      } yield {
          bigResult
      }
    }

  }

}
