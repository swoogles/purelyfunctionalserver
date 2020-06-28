package auth

import cats.Eq
import cats.implicits._
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

/*
In our example, we will demonstrate how to use SimpleAuthEnum, as well as
Role based authorization
 */
sealed case class Role(roleRepr: String)

object Role extends SimpleAuthEnum[Role, String] {

  val Administrator: Role = Role("Administrator")
  val Customer: Role = Role("User")
  val Seller: Role = Role("Seller")

  implicit val E: Eq[Role] = Eq.fromUniversalEquals[Role]

  protected val values: AuthGroup[Role] = AuthGroup(Administrator, Customer, Seller)

  //    override val getRepr: Role => String = role => getReprDef(role)
  //    override val orElse: Role = ???
  override def getRepr(t: Role): String = {
    println("hit getrepr")
    t.roleRepr
  }
}
