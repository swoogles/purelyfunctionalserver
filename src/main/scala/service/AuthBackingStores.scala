package service

import auth.OAuthLogic
import cats._
import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import tsec.authentication._
import tsec.authorization._

import scala.collection.mutable

object AuthBackingStores {
  def dummyBackingStore[F[_], I, V](getId: V => I)(implicit F: Sync[F]): BackingStore[F, I, V] =
    new BackingStore[F, I, V] {
      private val storageMap = mutable.HashMap.empty[I, V]

      def put(elem: V): F[V] = {
        val map = storageMap.put(getId(elem), elem)
        if (map.isEmpty)
          F.pure(elem)
        else
          F.raiseError(new IllegalArgumentException)
      }

      def getAndInsertIfNotPresent(id: I, elem: V) = {
        get (id).map {
          case Some(existingElement) =>  existingElement
          case None => put(elem)
        }
      }

      override def get(id: I): OptionT[F, V] = {
        //      storageMap.put(User(1, 10, "admin", Role.Administrator))
        println(s"Getting stored auth creds for id: $id")


        //      println(s"Getting stored auth creds for coerced id: ${SecureRandomId.coerce(id)}")
        println(s"retrieved user: " + storageMap.get(id))
        OptionT.fromOption[F](
          storageMap.get(id) match {
            case Some(creds) => Some(creds)
            case None => {
              println("No creds found. Inserting them next.")
              None
            }
          }
        )
      }

      def update(v: V): F[V] = {
        storageMap.update(getId(v), v)
        F.pure(v)
      }

      def delete(id: I): F[Unit] =
        storageMap.remove(id) match {
          case Some(_) => F.unit
          case None    => F.raiseError(new IllegalArgumentException)
        }
    }

  def realOauthStore[F[_], I, V](oauthLogic: OAuthLogic)(getId: V => I)(implicit F: Sync[F]): BackingStore[F, I, V] =
    new BackingStore[F, I, V] {
      private val storageMap = mutable.HashMap.empty[I, V]

      def put(elem: V): F[V] = {
        val map = storageMap.put(getId(elem), elem)
        if (map.isEmpty)
          F.pure(elem)
        else
          F.raiseError(new IllegalArgumentException)
      }

      def getAndInsertIfNotPresent(id: I, elem: V) = {
        get (id).map {
          case Some(existingElement) =>  existingElement
          case None => put(elem)
        }
      }

      override def get(id: I): OptionT[F, V] = {
        //      storageMap.put(User(1, 10, "admin", Role.Administrator))
        println(s"Getting stored auth creds for id: $id")


        //      println(s"Getting stored auth creds for coerced id: ${SecureRandomId.coerce(id)}")
        println(s"retrieved user: " + storageMap.get(id))
        OptionT.fromOption[F](
          storageMap.get(id) match {
            case Some(creds) => Some(creds)
            case None => {
              println("No creds found. Inserting them next.")
              None
            }
          }
        )
      }

      def update(v: V): F[V] = {
        storageMap.update(getId(v), v)
        F.pure(v)
      }

      def delete(id: I): F[Unit] =
        storageMap.remove(id) match {
          case Some(_) => F.unit
          case None    => F.raiseError(new IllegalArgumentException)
        }
    }

  /*
  In our example, we will demonstrate how to use SimpleAuthEnum, as well as
  Role based authorization
   */
  sealed case class Role(roleRepr: String)

  object Role extends SimpleAuthEnum[Role, String] {

    val Administrator: Role = Role("Administrator")
    val Customer: Role      = Role("User")
    val Seller: Role        = Role("Seller")

    implicit val E: Eq[Role] = Eq.fromUniversalEquals[Role]


    protected val values: AuthGroup[Role] = AuthGroup(Administrator, Customer, Seller)
    //    override val getRepr: Role => String = role => getReprDef(role)
    //    override val orElse: Role = ???
    override def getRepr(t: Role): String = {
      println("hit getrepr")
      t.roleRepr
    }
  }

  case class User(idInt: Int, age: Int, name: String, role: Role = Role.Customer)

  object User {
    implicit def authRole[F[_]](implicit F: MonadError[F, Throwable]): AuthorizationInfo[F, Role, User] =
      new AuthorizationInfo[F, Role, User] {
        def fetchInfo(u: User): F[Role] = {
          println("Anything?!")
          F.pure(u.role)
        }
      }
  }
}
