package db

import domain.User
import fs2.Stream

trait UserRepository[F[_]] {
  def create(user: User): F[Unit]
  def update(id: Long, user: User): F[Unit]
  def get(userId: Long): F[Option[User]]
  def list(role: String = "all"): Stream[F, User]
}
