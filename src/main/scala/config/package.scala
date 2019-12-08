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
  }
}
