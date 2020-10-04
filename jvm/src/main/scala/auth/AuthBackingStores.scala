package auth

import cats.data.OptionT
import cats.effect.Sync
import tsec.authentication.BackingStore

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

      def getAndInsertIfNotPresent(id: I, elem: V) =
        get(id).map {
          case Some(existingElement) => existingElement
          case None                  => put(elem)
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

  def realOauthStore[F[_], I, V](
    oauthLogic: AuthLogic
  )(getId: V => I)(implicit F: Sync[F]): BackingStore[F, I, V] =
    new BackingStore[F, I, V] {
      private val storageMap = mutable.HashMap.empty[I, V]

      def put(elem: V): F[V] = {
        val map = storageMap.put(getId(elem), elem)
        if (map.isEmpty)
          F.pure(elem)
        else
          F.raiseError(new IllegalArgumentException)
      }

      def getAndInsertIfNotPresent(id: I, elem: V) =
        get(id).map {
          case Some(existingElement) => existingElement
          case None                  => put(elem)
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

}
