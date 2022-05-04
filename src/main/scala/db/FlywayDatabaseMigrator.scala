package db

import cats.effect.IO
import org.flywaydb.core.Flyway

final class FlywayDatabaseMigrator extends DatabaseMigrator[IO] {
  override def migrate(url: String, user: String, password: String): IO[Unit] =
    IO {
      val flyway: Flyway = Flyway.configure().dataSource(url, user, password).load()
      flyway.migrate()
    }
}
