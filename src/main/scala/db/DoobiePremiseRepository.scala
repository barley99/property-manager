package db

import cats.effect.Sync
import domain.Premise
import doobie._
import doobie.implicits._
import fs2.Stream

class DoobiePremiseRepository[F[_]: Sync](xa: Transactor[F]) extends PremiseRepository[F] {
  override def create(key: String, premise: Premise): F[Unit] = ???

  override def update(id: Long, premise: Premise): F[Unit] = ???

  override def get(premiseId: Long): F[Option[Premise]] = ???

  override def list(
      isAvailable: Boolean,
      address: String,
      areaMin: Int,
      areaMax: Int,
      totalPriceMin: Double,
      totalPriceMax: Double
  ): Stream[F, Premise] = ???
}
