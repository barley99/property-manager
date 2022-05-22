import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import cats.effect.IO
import api.{Endpoints, PremisesEndpoints}
import com.typesafe.scalalogging.LazyLogging
import db.DoobiePremiseRepository
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

    val repository = new DoobiePremiseRepository[IO](xa)
    val service    = new PremisesEndpoints(repository, "api")

    val routes = AkkaHttpServerInterpreter().toRoute(service.allEndpoints)
    val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(
      service.allEndpoints,
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
