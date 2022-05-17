package db

import domain.Premise
import fs2.Stream

trait PremiseRepository[F[_]] {
  def create(key: String, premise: Premise): F[Unit]
  def update(id: Long, premise: Premise): F[Unit]
  def get(premiseId: Long): F[Option[Premise]]
  def list(
      isAvailable: Boolean = true,
      address: String = "",
      areaMin: Int = 0,
      areaMax: Int = Int.MaxValue,
      totalPriceMin: Double = 0,
      totalPriceMax: Double = Double.MaxValue,
  ): Stream[F, Premise]
}
