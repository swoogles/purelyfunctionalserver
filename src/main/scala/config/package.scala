import java.net.URI
import java.sql.Connection

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderException

package object config {
  case class ServerConfig(host: String ,port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String)

  case class Config(server: ServerConfig, database: DatabaseConfig)

  object Config {
    import pureconfig._
    import pureconfig.generic.auto._

    // TODO Manage this via environment variables
    // Heroku provides them in this format:
    // DATABASE_URL:         postgres://qmhxdoddwnnxxf:08629a4539f1d422eaa411d98cdd044411726505b6c3a7e56e796d60777073a5@ec2-107-22-211-248.compute-1.amazonaws.com:5432/da3c6qh0l04kkb
    def load(configFile: String = "application.conf"): IO[Config] = {
      IO {
        loadConfig[Config](ConfigFactory.load(configFile))
      }.flatMap {
        case Left(e) => IO.raiseError[Config](new ConfigReaderException[Config](e))
        case Right(config) => IO.pure(config)
      }
    }

    import java.net.URISyntaxException
    import java.sql.DriverManager
    import java.sql.SQLException


    private def getConnection = {
    }

    def loadDatabaseEnvironmentVariables(): IO[DatabaseConfig] = IO {
      val dbUri = new URI(System.getenv("DATABASE_URL"))
      val username = dbUri.getUserInfo.split(":")(0)
      val password = dbUri.getUserInfo.split(":")(1)
      val dbUrl = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath + "?sslmode=require"
      DriverManager.getConnection(dbUrl, username, password)
//      DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")
      DatabaseConfig("org.postgresql.Driver", dbUrl, username, password)
    }
  }
}
