import akka.actor.ActorSystem
import api.{ContractsEndpoints, PremisesEndpoints, UsersEndpoints}
import cats.effect.IO
import db.{DoobieContractRepository, DoobiePremiseRepository, DoobieUserRepository}
import doobie.Transactor
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.client3._

class BaseSpec extends AsyncFunSuite with Matchers {
  implicit val system = ActorSystem()
  implicit val es     = system.dispatcher

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost/postgres",
    "postgres",
    "postgres"
  )

  val basePath            = "api"
  val premisesRepository  = new DoobiePremiseRepository[IO](xa)
  val premisesService     = new PremisesEndpoints(premisesRepository, basePath)
  val usersRepository     = new DoobieUserRepository[IO](xa)
  val usersService        = new UsersEndpoints(usersRepository, basePath)
  val contractsRepository = new DoobieContractRepository[IO](xa)
  val contractsService    = new ContractsEndpoints(contractsRepository, basePath)

  val endpoints = premisesService.allEndpoints ++ usersService.allEndpoints ++ contractsService.allEndpoints

  SttpClientInterpreter().toClient(usersService.getUserEndpoint, Some(uri"http://localhost:8080"), SttpB)
  usersService.getUserEndpoint.toRe(baseUri).apply()

}
