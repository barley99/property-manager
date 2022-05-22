package db

import domain.Contract
import fs2.Stream

trait ContractRepository[F[_]] {
  def create(contract: Contract): F[Unit]
  def update(id: Long, contract: Contract): F[Unit]
  def get(contractId: Long): F[Option[Contract]]
  def list(
      active: Boolean = true,
      tenantId: Option[Long]
  ): Stream[F, Contract]
}
