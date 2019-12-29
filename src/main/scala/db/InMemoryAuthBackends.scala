package db

import cats.effect.IO
import service.AuthHelpers.{User, dummyBackingStore}
import tsec.authentication.{BackingStore, TSecBearerToken}
import tsec.common.SecureRandomId

object InMemoryAuthBackends {
  val bearerTokenStoreThatShouldBeInstantiatedOnceByTheServer: BackingStore[IO, SecureRandomId, TSecBearerToken[Int]] =
    dummyBackingStore[IO, SecureRandomId, TSecBearerToken[Int]](tokenValue => {// This function is: Int => SecureRandomId
      println(s"Turning s.id: ${tokenValue.id} into a SecureRandomId: ${SecureRandomId.coerce(tokenValue.id) }")
      SecureRandomId.coerce(tokenValue.id) // TODO Restore as entire body of this function
    })

  //We create a way to store our users. You can attach this to say, your doobie accessor
  val userStoreThatShouldBeInstantiatedOnceByTheServer: BackingStore[IO, Int, User] =
    dummyBackingStore[IO, Int, User](
      getId = (user: User) =>  user.idInt
    ) //This function is: User => Int
}
