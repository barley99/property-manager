package db

import cats.effect.Sync
import cats.free.Free
import cats.implicits._
import domain.{Building, Premise}
import doobie._
import doobie.free.connection
import doobie.implicits._
import doobie.postgres._
import fs2.Stream

class DoobiePremiseRepository[F[_]: Sync](xa: Transactor[F]) extends PremiseRepository[F] {

  override def listBuildings(addressLike: Option[String]): Stream[F, Building] =
    Q.selectBuildings(addressLike: Option[String]).take(100).transact(xa)

  override def create(premise: Premise): F[Unit] =
    Q.insert(premise).transact(xa)

  override def update(id: Long, premise: Premise): F[Unit] =
    Q.update(id, premise).transact(xa)

  override def get(id: Long): F[Option[Premise]] =
    Q.selectById(id).transact(xa)

  override def list(
      isAvailable: Boolean = true,
      address: Option[String],
      areaMin: Option[Int],
      areaMax: Option[Int],
      totalPriceMin: Option[Int],
      totalPriceMax: Option[Int]
  ): Stream[F, Premise] =
    Q.selectAllWithFilter(isAvailable, address, areaMin, areaMax, totalPriceMin, totalPriceMax).transact(xa)

  object Q {

    def selectBuildings(addressLike: Option[String]): Stream[ConnectionIO, Building] = {
      val whereFragment = addressLike.fold(fr"")(s => fr"WHERE address LIKE ${"%" + s + "%"}")
      val mainFragment =
        sql"""
             |SELECT id, address FROM Buildings
             |""".stripMargin
      (mainFragment ++ whereFragment ++ fr"ORDER BY address").query[Building].stream
    }

    def getBuildingByAddress(address: String): ConnectionIO[Option[Building]] =
      sql"""
           |SELECT id, address FROM Buildings WHERE address = $address LIMIT 1
           |""".stripMargin.query[Building].option

    def insertBuilding(address: String): ConnectionIO[Building] =
      sql"""
           |INSERT INTO Buildings (address) VALUES ($address)
           |""".stripMargin.update.withUniqueGeneratedKeys[Building]("id", "address")

    def insert(premise: Premise): Free[connection.ConnectionOp, Unit] = {
      for {
        buildingOption <- getBuildingByAddress(premise.address)
        building       <- buildingOption.fold(insertBuilding(premise.address))(_.pure[ConnectionIO])
        _ <-
          sql"""
               |INSERT INTO Premises (building_id, landlord_id, floor, number, area, description, advertised_price)
               |VALUES (${building.id}, ${premise.landlordId}, ${premise.floor}, 
               |         ${premise.number}, ${premise.area}, ${premise.description}, ${premise.advertisedPrice})
               |""".stripMargin.update.run.attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
            "Ошибка: В этом здании на указаном этаже уже есть помещение с таким номером."
          }
      } yield ()
    }

    def update(id: Long, premise: Premise): Free[connection.ConnectionOp, Unit] = {
      for {
        buildingOption <- getBuildingByAddress(premise.address)
        building       <- buildingOption.fold(insertBuilding(premise.address))(_.pure[ConnectionIO])
        _ <-
          sql"""
                 |UPDATE Premises 
                 |SET building_id = ${building.id},
                 |    landlord_id = ${premise.landlordId},
                 |    floor = ${premise.floor}, 
                 |    number = ${premise.number},
                 |    area = ${premise.area},
                 |    description = ${premise.description}, 
                 |    advertised_price = ${premise.advertisedPrice}
                 |WHERE id = $id
                 |""".stripMargin.update.run
      } yield ()
    }

    def selectById(id: Long): ConnectionIO[Option[Premise]] =
      sql"""
        |SELECT P.id, address, landlord_id, floor, number, area, description, advertised_price
        |FROM Premises AS P
        |INNER JOIN Buildings AS B ON P.building_id = B.id
        |WHERE P.id = $id
        |""".stripMargin.query[Premise].option

    def selectAllWithFilter(
        isAvailable: Boolean,
        address: Option[String],
        areaMin: Option[Int],
        areaMax: Option[Int],
        totalPriceMin: Option[Int],
        totalPriceMax: Option[Int]
    ): Stream[doobie.ConnectionIO, Premise] = {

      val frAddress       = address.fold(fr"")(s => fr" AND address LIKE ${"%" + s + "%"}")
      val frAreaMin       = areaMin.fold(fr"")(s => fr" AND P.area >= $s::INT")
      val frAreaMax       = areaMax.fold(fr"")(s => fr" AND P.area <= $s::INT")
      val frTotalPriceMin = totalPriceMin.fold(fr"")(s => fr" AND P.advertised_price >= $s::INT")
      val frTotalPriceMax = totalPriceMax.fold(fr"")(s => fr" AND P.advertised_price <= $s::INT")

      val today: String = java.time.LocalDate.now.toString

      // Считаем доступными помещения, не участвующие в договорах на сегодня или в следующие два календарных месяца
      val frAvailable: Fragment = {
        if (!isAvailable) fr""
        else
          fr"""
          |AND NOT EXISTS (
          |   SELECT * 
          |   FROM PremisesInContracts AS PC 
          |   WHERE P.id = PC.premise_id 
          |     AND ((PC.start_dt < $today::DATE AND PC.end_dt > $today::DATE) 
          |         OR (PC.start_dt < date_trunc('month', $today::DATE + interval '3 months' )))  
          |)
          |""".stripMargin
      }

      val whereFragment: Fragment =
        fr"WHERE (1=1)" ++ frAddress ++ frAreaMin ++ frAreaMax ++ frTotalPriceMin ++ frTotalPriceMax // ++ frAvailable

      val mainFragment = sql"""
             |SELECT P.id, B.address, P.landlord_id, P.floor, P.number, P.area, P.description, P.advertised_price 
             |FROM Premises AS P
             |INNER JOIN Buildings AS B ON P.building_id = B.id
             |""".stripMargin
      (mainFragment ++ whereFragment ++ fr"ORDER BY P.id DESC").query[Premise].stream
    }
  }

}
