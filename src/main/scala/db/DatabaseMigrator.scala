abstract class DatabaseMigrator[F[_]] {
  def migrate(url: String, user: String, password: String): F[Int]
}
