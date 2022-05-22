package domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class Building(
    id: Long,
    address: String
)

object Building {
  implicit val jsonDecoder: Decoder[Building] = deriveDecoder
  implicit val jsonEncoder: Encoder[Building] = deriveEncoder
}
