package db

import cats.effect.{Blocker, IO, Resource}
import config.DatabaseConfig
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext.global
object Database {
  private implicit val cs = IO.contextShift(global)
  def transactor(config: DatabaseConfig): Resource[IO, HikariTransactor[IO]] = {

    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password, global,Blocker.liftExecutionContext(global))
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
