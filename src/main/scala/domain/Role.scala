package domain

import cats._
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

final case class Role(roleRepr: String)

object Role extends SimpleAuthEnum[Role, String] {
  val Manager: Role = Role("Manager")
  val Tenant: Role  = Role("Tenant")

  override val values: AuthGroup[Role] = AuthGroup(Manager, Tenant)

  override def getRepr(t: Role): String = t.roleRepr

  implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals[Role]
}
