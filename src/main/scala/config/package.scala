import java.net.URI
import cats.effect.Sync
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderFailures

package object config {
  trait Config[F[_]] {
    def load(configFile: String = "application.conf"): F[ConfigData]
    def loadDatabaseEnvironmentVariables(): F[config.DatabaseConfig]
  }
  case class ServerConfig(host: String ,port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String)

  case class ConfigData(server: ServerConfig, database: DatabaseConfig)

  object Config {
    import pureconfig._
    def apply[F[_]](implicit ev: Config[F]): Config[F] = ev
    import pureconfig.generic.auto._

    // TODO Manage this via environment variables
    // Heroku provides them in this format:
    // DATABASE_URL:         postgres://qmhxdoddwnnxxf:08629a4539f1d422eaa411d98cdd044411726505b6c3a7e56e796d60777073a5@ec2-107-22-211-248.compute-1.amazonaws.com:5432/da3c6qh0l04kkb
    def impl[F[_]: Sync](
                          raiseError: ( ConfigReaderFailures) => F[ConfigData],
                          pure: (ConfigData) =>F[ConfigData],
                          pureDatabaseConfig: (DatabaseConfig) =>F[DatabaseConfig],
                          raiseErrorForDatabaseConfig: ( NullPointerException) => F[DatabaseConfig]
                        ): Config[F] = new Config[F] {

      def load(configFile: String = "application.conf"): F[ConfigData] = {
        ConfigSource.fromConfig(ConfigFactory.load(configFile)).load[ConfigData]
          match {
            case Left(e) => raiseError(e)
            case Right(config) => pure(config)
          }
      }

      import java.sql.DriverManager

      def loadDatabaseEnvironmentVariables(): F[DatabaseConfig] = {
        try {
          val dbUri = new URI(System.getenv("DATABASE_URL"))
          val username = dbUri.getUserInfo.split(":")(0)
          val password = dbUri.getUserInfo.split(":")(1)
          val dbUrl = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath + "?sslmode=require"
          DriverManager.getConnection(dbUrl, username, password)
          //      DatabaseConfig("org.postgresql.Driver", "jdbc:postgresql:doobie", "postgres", "password")
          pureDatabaseConfig(DatabaseConfig("org.postgresql.Driver", dbUrl, username, password))
        } catch { case nullPointerException: NullPointerException => raiseErrorForDatabaseConfig(nullPointerException)}
      }
    }
  }
}
