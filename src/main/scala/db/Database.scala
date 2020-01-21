package db

import cats.effect.{Blocker, ContextShift, IO, Resource}
import config.DatabaseConfig
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import zio.Task
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object Database {
  def transactor(config: DatabaseConfig)(ec: ExecutionContext): Resource[Task, HikariTransactor[Task]] = {

    Blocker.liftExecutionContext(ec)
    implicit val cs: ContextShift[Task] = zio.interop.catz.zioContextShift
    implicit val async = zio.interop.catz.taskConcurrentInstance
    HikariTransactor.newHikariTransactor[Task](
      config.driver,
      config.url,
      config.user,
      config.password,
      ec,
      Blocker.liftExecutionContext(ec)
    )
  }

  def initialize(transactor: HikariTransactor[Task]): Task[Unit] = {
    transactor.configure { dataSource =>
      Task[Unit] {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }
  }
}
