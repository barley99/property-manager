import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import api.Endpoints
import com.typesafe.scalalogging.LazyLogging
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future

object EndpointsServer extends LazyLogging {
  def main(args: Array[String]) = {
    implicit val system = ActorSystem()
    implicit val es     = system.dispatcher

    val service = new Endpoints

    val routes = AkkaHttpServerInterpreter().toRoute(service.allEndpoints)
    val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(
      service.allEndpoints,
      "coffee server",
      "0.0.1"
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
