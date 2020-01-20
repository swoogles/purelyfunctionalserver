package service

import java.io.File

import cats.effect._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.staticcontent._
import org.http4s.{HttpRoutes, StaticFile}

class HomePageService(blocker: Blocker)(implicit cs: ContextShift[IO], ev: Effect[IO]) extends Http4sDsl[IO] {
  val rootFileService: HttpRoutes[IO] = fileService[IO](FileService.Config(".", blocker))

  val routes = HttpRoutes.of[IO] {
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
