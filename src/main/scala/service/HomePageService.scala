package service

import java.io.File

import cats.effect._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.staticcontent._
import org.http4s.{HttpRoutes, StaticFile}

class HomePageService[F[_] : Sync](blocker: Blocker)(implicit cs: ContextShift[F], ev: Effect[F]) extends Http4sDsl[F] {
  val rootFileService: HttpRoutes[F] = fileService[F](FileService.Config(".", blocker))

  val routes = HttpRoutes.of[F] {
    case request@GET -> Root => {
      val servedFile = new File("./src/main/resources/html/index.html")
      println("got a file. dunno if it exists")
      println(servedFile.exists())
      println(servedFile.getAbsolutePath)

      StaticFile.fromFile(servedFile, blocker, Some(request))
        .getOrElseF(NotFound()) // In case the file doesn't exist
    }
  }
}
