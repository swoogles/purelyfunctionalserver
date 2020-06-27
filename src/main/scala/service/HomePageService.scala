package service

import java.io.File

import cats.effect._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.staticcontent._
import org.http4s.{HttpRoutes, StaticFile}
import zio.Task
import zio.interop.catz._
import zio.interop.catz.implicits._

class HomePageService(blocker: Blocker)(implicit cs: ContextShift[Task]) extends Http4sDsl[Task] {
//  val rootFileService: HttpRoutes[Task] = fileService[Task](FileService.Config(".", blocker))

  val routes = HttpRoutes.of[Task] {
    case request @ GET -> Root => {
      val servedFile = new File("./src/main/resources/html/landing.html")
      println("got a file. dunno if it exists")
      println(servedFile.exists())
      println(servedFile.getAbsolutePath)

      StaticFile
        .fromFile[Task](servedFile, blocker, Some(request))
        .getOrElseF(NotFound()) // In case the file doesn't exist
    }
  }
}
