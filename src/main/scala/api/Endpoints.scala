package api

import domain.{Contract, Premise, User}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.Info

class Endpoints {

  private val info = Info(
    "Property Management Application - Backend",
    "0.1",
  )

  private val basePath = "api"
//  val docsYaml: String = docs.toYaml

  // Registration: POST /api/users
  private val addUserEndpoint =
    endpoint.post
      .in(basePath / "users")
      .in(jsonBody[User].description("Info for new user registration"))
      .errorOut(stringBody)
      .description("Create new user")

  // Get Tenant or Landlord: GET /api/user/{id}
  private val getUserEndpoint =
    endpoint.get
      .in(basePath / "user" / path[Long]("id").description("User ID"))
//      .out(jsonBody[User])
      .errorOut(stringBody)
//      .serverLogic[Future] { _ => Future.successful(Right(())) }
      .description("Get info about tenant/landlord/user by id")

  // Update Tenant or Landlord: PUT /api/user/{id}
  private val updateUserInfo =
    endpoint.patch
      .in(basePath / "user" / path[Long]("id").description("User ID"))
      .in(jsonBody[User])
      .errorOut(stringBody)
      .description("Update user info by id, changed phone number for example")

  // Add Premise: POST /api/premises
  private val newPremiseEndpoint =
    endpoint.post
      .in(basePath / "premises")
      .in(jsonBody[Premise].description("Info about new premise and building where it's located"))
      .errorOut(stringBody)
      .description("Create new premise record")

  // Get Premise: GET /api/premise/{id}
  private val getPremiseEndpoint =
    endpoint.get
      .in(basePath / "premise" / path[Long]("id").description("Premise ID"))
      .out(jsonBody[Premise])
      .errorOut(stringBody)
      .description("Get info about premise by id")

  // Update Premise: PUT /api/premise/{id}
  private val updatePremiseEndpoint =
    endpoint.patch
      .in(basePath / "premise" / path[Long]("id").description("Premise ID"))
      .in(jsonBody[Premise])
      .errorOut(stringBody)
      .description("Update premise by id")

  // List Premises: GET /api/premises
  // List Premises: GET /api/premises?areamin=15&areamax=25
  // List Premises: GET /api/premises?address=Тюленина, 100
  // List Premises: GET /api/premises?totalpricemin=10000&totalpricemax=20000
  // List Premises available for lease: GET /api/premises?available=true
  private val listPremisesEndpoint =
    endpoint.get
      .in(
        basePath / "premises"
          / query[Int]("areamin").description("Minimal area of filtered premises")
          / query[Int]("areamax").description("Maximal area of filtered premises")
          / query[Int]("totalpricemin").description("Minimal price per month of filtered premises")
          / query[Int]("totalpricemax").description("Maximal price per month of filtered premises")
          / query[Boolean]("isavailaible").default(true).description("Premises are available for lease")
          / query[String]("address").description("Substring of address of building")
      )
      .out(jsonBody[List[Premise]])
      .errorOut(stringBody)
      .description("Get list of premises with filtering")

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
      .in(
        basePath / "contracts"
          / query[Boolean]("active").default(true).description("Contracts active on current date")
          / query[Long]("tenant").description("Filter by tenant id")
      )
      .out(jsonBody[List[Contract]])
      .errorOut(stringBody)
      .description("Get list of contracts")

  val allEndpoints = List(
//    addUserEndpoint,
    getUserEndpoint,
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