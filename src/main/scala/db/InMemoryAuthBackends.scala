package db

import cats.effect.IO
import service.AuthBackingStores.dummyBackingStore
import service.User
import tsec.authentication.{BackingStore, TSecBearerToken}
import tsec.common.SecureRandomId
import zio.Task
import zio.interop.catz._

object InMemoryAuthBackends {

  val bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer
    : BackingStore[Task, SecureRandomId, TSecBearerToken[Int]] =
    dummyBackingStore[Task, SecureRandomId, TSecBearerToken[Int]](
      tokenValue => { // This function is: Int => SecureRandomId
        println(
          s"Turning s.id: ${tokenValue.id} into a SecureRandomId: ${SecureRandomId.coerce(tokenValue.id)}"
        )
        SecureRandomId.coerce(tokenValue.id) // TODO Restore as entire body of this function
      }
    )

  //We create a way to store our users. You can attach this to say, your doobie accessor
  val userStoreThatShouldBeInstantiatedOnceByTheServer: BackingStore[Task, Int, User] =
    dummyBackingStore[Task, Int, User](
      getId = (user: User) => user.idInt
    ) //This function is: User => Int
}
