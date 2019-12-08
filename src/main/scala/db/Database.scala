package db

import java.util.concurrent.Executors

import cats.effect.{Blocker, IO, Resource}
import config.DatabaseConfig
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

object Database {
  def transactor(config: DatabaseConfig)(ec: ExecutionContext): Resource[IO, HikariTransactor[IO]] = {

    Blocker.liftExecutionContext(ec)
    implicit val cs = IO.contextShift(ec)
    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password,
      ec,
      Blocker.liftExecutionContext(ec)
    )
  }

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }
  }
}
