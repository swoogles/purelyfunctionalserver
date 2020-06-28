package service

import auth.OAuthLogic
import cats._
import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import tsec.authentication._
import tsec.authorization._

case class User(idInt: Int, age: Int, name: String, role: Role = Role.Customer)

object User {

  implicit def authRole[F[_]](
                               implicit F: MonadError[F, Throwable]
                             ): AuthorizationInfo[F, Role, User] =
    new AuthorizationInfo[F, Role, User] {

      def fetchInfo(u: User): F[Role] = {
        println("Anything?!")
        F.pure(u.role)
      }
    }
}
