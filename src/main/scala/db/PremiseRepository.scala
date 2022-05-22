package db

import domain.{Building, Premise}
import fs2.Stream

trait PremiseRepository[F[_]] {
  def listBuildings(addressLike: Option[String]): Stream[F, Building]
  def create(premise: Premise): F[Unit]
  def update(id: Long, premise: Premise): F[Unit]
  def get(premiseId: Long): F[Option[Premise]]
  def list(
      isAvailable: Boolean = true,
      address: Option[String],
      areaMin: Option[Int],
      areaMax: Option[Int],
      totalPriceMin: Option[Int],
      totalPriceMax: Option[Int]
  ): Stream[F, Premise]
}
