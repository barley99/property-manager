package db

import cats.effect.Sync
import cats.implicits._
import domain.User
import doobie._
import doobie.implicits._

class DoobieUserRepository[F[_]: Sync](xa: Transactor[F]) extends UserRepository[F] {
  override def create(user: User): F[Unit] =
    sql"""
      |INSERT INTO Users (first_name, last_name, email, phone, passwd, role)
      |VALUES (${user.firstName}, ${user.lastName}, ${user.email}, ${user.phone}, ${user.hash}, ${user.role})
      |""".stripMargin.update.run.transact(xa).map(_ => ())

  override def update(id: Long, user: User): F[Unit] =
    sql"""
         |UPDATE Users 
         |SET first_name = ${user.firstName}, 
         |    last_name = ${user.lastName},
         |    email = ${user.email}, 
         |    phone = ${user.phone}, 
         |    passwd = ${user.hash}, 
         |    role = ${user.role}
         |WHERE id = $id
         |""".stripMargin.update.run.transact(xa).map(_ => ())

  override def get(userId: Long): F[Option[User]] =
    sql"""
         |SELECT id, first_name, last_name, email, phone, passwd, role
         |FROM Users
         |WHERE id = $userId
         |""".stripMargin.query[User].option.transact(xa)

  override def list(role: String): fs2.Stream[F, User] = {
    val filter =
      if (role != "all") fr"WHERE role = $role"
      else fr""

    val mainFragment =
      sql"""
        |SELECT id, first_name, last_name, email, phone, passwd, role
        |FROM Users
        |""".stripMargin

    (mainFragment ++ filter ++ fr"ORDER BY last_name, first_name").query[User].stream.take(100).transact(xa)
  }
}
