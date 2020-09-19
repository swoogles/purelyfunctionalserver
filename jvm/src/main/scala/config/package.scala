import java.net.URI

import cats.effect.{IO, Sync}
import com.typesafe.config.ConfigFactory
import pureconfig.error.{ConfigReaderException, ConfigReaderFailures}
import cats.implicits._
import config.ConfigData
import zio.{Task, ZIO}

package object config {

  trait Config {
    //    def load(configFile: String = "application.conf"): IO[ConfigData]
    //    def loadDatabaseEnvironmentVariables(): IO[config.DatabaseConfig]
    def configSteps(): Task[ConfigData]
  }
  case class ServerConfig(host: String, port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String)

  case class ConfigData(server: ServerConfig, database: DatabaseConfig)

  object Config {
    import pureconfig._
    import pureconfig.generic.auto._

    // TODO Manage this via environment variables
    // Heroku provides them in this format:
    // DATABASE_URL:         postgres://qmhxdoddwnnxxf:08629a4539f1d422eaa411d98cdd044411726505b6c3a7e56e796d60777073a5@ec2-107-22-211-248.compute-1.amazonaws.com:5432/da3c6qh0l04kkb
    def impl(): Config = new Config {

      def configSteps(): Task[ConfigData] =
        load().flatMap { configFromFile =>
          loadDatabaseEnvironmentVariables()
            .map(envDbConfig => configFromFile.copy(database = envDbConfig))
            .orElse(Task.succeed(configFromFile))
        }

      protected def load(configFile: String = "application.conf"): Task[ConfigData] =
        ConfigSource.fromConfig(ConfigFactory.load(configFile)).load[ConfigData] match {
          case Left(e)       => Task.fail(new ConfigReaderException[ConfigData](e))
          case Right(config) => Task.succeed(config)
        }

      import java.sql.DriverManager

      protected def loadDatabaseEnvironmentVariables(): Task[DatabaseConfig] =
        try {
          val dbUri = new URI(System.getenv("DATABASE_URL"))
          val username = dbUri.getUserInfo.split(":")(0)
          val password = dbUri.getUserInfo.split(":")(1)
          val dbUrl = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath + "?sslmode=require"
          DriverManager.getConnection(dbUrl, username, password)
          //      DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")
          Task(DatabaseConfig("org.postgresql.Driver", dbUrl, username, password))
        } catch {
          case nullPointerException: NullPointerException => Task.fail(nullPointerException)
        }

    }
  }
}
