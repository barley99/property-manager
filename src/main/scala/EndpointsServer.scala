import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import cats.effect.IO
import api.{Endpoints, PremisesEndpoints, UsersEndpoints}
import com.typesafe.scalalogging.LazyLogging
import db.{DoobiePremiseRepository, DoobieUserRepository}
import doobie.Transactor
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future

object EndpointsServer extends LazyLogging {
  def main(args: Array[String]) = {
    implicit val system = ActorSystem()
    implicit val es     = system.dispatcher

    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost/postgres",
      "postgres",
      "postgres"
    )

    val premisesRepository = new DoobiePremiseRepository[IO](xa)
    val premisesService    = new PremisesEndpoints(premisesRepository, "api")
    val usersRepository    = new DoobieUserRepository[IO](xa)
    val usersService       = new UsersEndpoints(usersRepository, "api")

    val routes = AkkaHttpServerInterpreter().toRoute(premisesService.allEndpoints ++ usersService.allEndpoints)
    val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(
      premisesService.allEndpoints ++ usersService.allEndpoints,
      "Property Management Server",
      "0.1"
    )

    val swagger =
      AkkaHttpServerInterpreter().toRoute(SwaggerUI[Future](openApi.toYaml))

    import akka.http.scaladsl.server.RouteConcatenation._

    Http()
      .newServerAt("localhost", 8080)
      .bind(routes ~ swagger)
      .foreach(binding => println(binding.toString))
  }
}
