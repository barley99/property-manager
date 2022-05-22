package domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class Premise(
    id: Option[Long],
    address: String,
    landlordId: Long,
    floor: Int,
    number: String,
    area: Double,
    description: Option[String],
    advertisedPrice: Int
)

object Premise {
  implicit val jsonDecoder: Decoder[Premise] = deriveDecoder
  implicit val jsonEncoder: Encoder[Premise] = deriveEncoder
}
