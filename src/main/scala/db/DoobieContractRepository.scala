package db

import cats.effect.Sync
import domain.Contract
import doobie._
import doobie.implicits._
import doobie.implicits.legacy.localdate._

import java.time.LocalDate

class DoobieContractRepository[F[_]: Sync](xa: Transactor[F]) extends ContractRepository[F] {
  override def create(contract: Contract): F[Unit] = {

    def reservePremises(
        contractId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        premisesPrices: List[(Long, Double)]
    ) =
      premisesPrices.map { case (premiseId, price) => (contractId, premiseId, price, startDate, endDate) }

    val premisesPricesSql =
      """
      |INSERT INTO PremisesInContracts (contract_id, premise_id, price, start_dt, end_dt)
      |VALUES (?, ?, ?, ?, ?)
      |""".stripMargin

    val transaction = for {
      id <- sql"""
             |INSERT INTO Contracts (number, agreement_dt, tenant_id, start_dt, end_dt, payment_dt, utilities_in_price)
             |VALUES (${contract.number}, ${contract.agreementDate}, ${contract.tenant}, ${contract.startDate},
             |        ${contract.endDate}, ${contract.paymentDate}, ${contract.utilitiesInPrice})
             |""".stripMargin.update.withUniqueGeneratedKeys[Int]("id")
      _ <- Update[(Long, Long, Double, LocalDate, LocalDate)](premisesPricesSql)
        .updateMany(reservePremises(
          id,
          contract.startDate,
          contract.endDate,
          contract.premisesPrices.getOrElse(Nil)
        ))
    } yield ()

    transaction.transact(xa)
  }

//  override def update(id: Long, contract: Contract): F[Unit] = ???

//  override def get(contractId: Long): F[Option[Contract]] = {
//    sql"""
//      |SELECT id, number, agreement_dt, tenant_id, start_dt, end_dt, payment_dt, utilities_in_price, NULL
//      |FROM Contracts
//      |WHERE id = $contractId
//      |""".stripMargin
////      .query[(Long, String, String, Long, String, String, String, Boolean)]
//      .query[Contract]
//      .option.transact(xa)
////      .query[(Long, String, LocalDate, Long, LocalDate, LocalDate, LocalDate, Boolean)]
//  }

//  override def list(active: Boolean, tenantId: Option[Long]): fs2.Stream[F, Contract] = ???

}
