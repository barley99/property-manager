package domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class Premise(
    id: Option[Long],
    city: String,
    address: String,
    buildingName: String,
    landlordName: String,
    floor: Int,
    number: String,
    area: Double,
    description: Option[String],
    advertisedPrice: Double
)

object Premise {
  implicit val jsonDecoder: Decoder[Premise] = deriveDecoder
  implicit val jsonEncoder: Encoder[Premise] = deriveEncoder
}
