package api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import db.{PremiseRepository, UserRepository}
import domain.{Building, Contract, Premise, User}
import sttp.model.StatusCode._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.Info

import scala.concurrent.{ExecutionContext, Future}

class PremisesEndpoints(db: PremiseRepository[IO], basePath: String)(implicit ec: ExecutionContext) {

//  implicit lazy val sBuilding: Schema[Building] = Schema.derived
//  implicit lazy val sPremise: Schema[Premise]   = Schema.derived

  // List available buildings: GET /api/buildings
  private val listBuildingsEndpoint =
    endpoint.get
      .in(basePath / "premises" / "buildings")
      .in(query[Option[String]]("addresslike"))
      .out(jsonBody[List[Building]])
      .errorOut(stringBody)
      .serverLogic[Future](address => db.listBuildings(address).compile.toList.unsafeToFuture().map(Right(_)))
      .description("Get list of buildings with optional filtering by susbstring")

  // Add Premise: POST /api/premises
  private val newPremiseEndpoint =
    endpoint.post
      .in(basePath / "premises")
      .in(jsonBody[Premise].description("Info about new premise and building where it's located"))
      .errorOut(stringBody)
      .serverLogic[Future](item =>
        db.create(item).unsafeToFuture().map(Right(_))
      )
      .description("Create new premise record")

  // Get Premise: GET /api/premise/{id}
  private val getPremiseEndpoint =
    endpoint.get
      .in(basePath / "premise" / path[Long]("id").description("Premise ID"))
      .out(jsonBody[Premise])
      .errorOut(stringBody)
      .serverLogic[Future](id => db.get(id).unsafeToFuture().map(_.toRight(NotFound.toString())))
      .description("Get info about premise by id")

  // Update Premise: PUT /api/premise/{id}
  private val updatePremiseEndpoint =
    endpoint.patch
      .in(basePath / "premise" / path[Long]("id").description("Premise ID"))
      .in(jsonBody[Premise])
      .errorOut(stringBody)
      .serverLogic[Future] { case (id, item) => db.update(id, item).unsafeToFuture().map(Right(_)) }
      .description("Update premise by id")

  // List Premises: GET /api/premises
  // List Premises: GET /api/premises?areamin=15&areamax=25
  // List Premises: GET /api/premises?address=Тюленина, 100
  // List Premises: GET /api/premises?totalpricemin=10000&totalpricemax=20000
  // List Premises available for lease: GET /api/premises?available=true

  private val listPremisesEndpoint =
    endpoint.get
      .in(basePath / "premises")
      .in(query[Boolean]("isavailable").default(true).description(
        "Premises are available for lease in next 1 month or longer"
      ))
      .in(query[Option[String]]("address").description("Substring of address of building"))
      .in(query[Option[Int]]("areamin").description("Minimal area of filtered premises"))
      .in(query[Option[Int]]("areamax").description("Maximal area of filtered premises"))
      .in(query[Option[Int]]("totalpricemin").description("Minimal price per month of filtered premises"))
      .in(query[Option[Int]]("totalpricemax").description("Maximal price per month of filtered premises"))
      .out(jsonBody[List[Premise]])
      .errorOut(stringBody)
      .serverLogic[Future] { queryParams =>
        (db.list _).tupled(queryParams).compile.toList.unsafeToFuture().map(Right(_))
      }
      .description("Get list of premises with filtering")

  val allEndpoints =
    List(listBuildingsEndpoint, newPremiseEndpoint, getPremiseEndpoint, updatePremiseEndpoint, listPremisesEndpoint)
}

class UsersEndpoints(db: UserRepository[IO], basePath: String)(implicit ec: ExecutionContext) {

  // Registration: POST /api/users
  private val addUserEndpoint =
    endpoint.post
      .in(basePath / "users")
      .in(jsonBody[User].description("Info for new user registration"))
      .errorOut(stringBody)
      .serverLogic[Future](db.create(_).unsafeToFuture().map(Right(_)))
      .description("Create new user")

  // Get Tenant or Landlord: GET /api/user/{id}
  private val getUserEndpoint =
    endpoint.get
      .in(basePath / "user" / path[Long]("id").description("User ID"))
      .out(jsonBody[User])
      .errorOut(stringBody)
      .serverLogic[Future](db.get(_).unsafeToFuture().map(_.toRight(NotFound.toString())))
      .description("Get info about tenant/landlord/user by id")

  // Update Tenant or Landlord: PUT /api/user/{id}
  private val updateUserEndpoint =
    endpoint.patch
      .in(basePath / "user" / path[Long]("id").description("User ID"))
      .in(jsonBody[User])
      .serverLogic[Future]((db.update _).tupled(_).unsafeToFuture().map(Right(_)))
      .description("Update user info by id, changed phone number for example")

  // List available buildings: GET /api/users?role=all
  private val listUsersEndpoint =
    endpoint.get
      .in(basePath / "users")
      .in(query[String]("role").default("all"))
      .out(jsonBody[List[User]])
      .errorOut(stringBody)
      .serverLogic[Future](db.list(_).compile.toList.unsafeToFuture().map(Right(_)))
      .description("Get list of users by role")

  val allEndpoints =
    List(addUserEndpoint, getUserEndpoint, updateUserEndpoint, listUsersEndpoint)
}

class Endpoints {

  private val info = Info(
    "Property Management Application - Backend",
    "0.1",
  )

  private val basePath = "api"
//  val docsYaml: String = docs.toYaml

  // Add Contract: POST /api/contracts
  private val newContractEndpoint =
    endpoint.post
      .in(basePath / "contracts")
      .in(jsonBody[Contract].description("Info about new contract"))
      .errorOut(stringBody)
      .description("Create new contract record")

  // Get Contract: GET /api/contract/{id}
  private val getContractEndpoint =
    endpoint.get
      .in(basePath / "contract" / path[Long]("id").description("Contract ID"))
      .out(jsonBody[Contract])
      .errorOut(stringBody)
      .description("Get info about contract by id")

  // Update Contract: PUT /api/contract/{id}
  private val updateContractEndpoint =
    endpoint.patch
      .in(basePath / "contract" / path[Long]("id").description("Contract ID"))
      .in(jsonBody[Contract])
      .errorOut(stringBody)
      .description("Update contract by id")

  // Delete Contract if current date < start_date: DELETE /api/contract/{id}
  private val deleteContractEndpoint =
    endpoint.delete
      .in(basePath / "contract" / path[Long]("id").description("Contract ID"))
      .out(jsonBody[Contract])
      .errorOut(stringBody)
      .description("Delete Contract by id if current date less than start_date, otherwise fail")

  // List current contracts: GET /api/contracts
  private val listContractsEndpoint =
    endpoint.get
      .in(basePath / "contracts")
      .in(query[Boolean]("active").default(true).description("Contracts active on current date"))
      .in(query[Option[Long]]("tenant").description("Filter by tenant id"))
      .out(jsonBody[List[Contract]])
      .errorOut(stringBody)
      .description("Get list of contracts")

  val allEndpoints = List(
//    addUserEndpoint,
//    getUserEndpoint,
//    updateUserInfo,
//    newPremiseEndpoint,
//    getPremiseEndpoint,
//    updatePremiseEndpoint,
//    listPremisesEndpoint,
//    newContractEndpoint,
//    getContractEndpoint,
//    updateContractEndpoint,
//    deleteContractEndpoint,
//    listContractsEndpoint
  )

}
